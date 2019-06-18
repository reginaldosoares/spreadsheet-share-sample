package io.golayer.app.domain.subscriber

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.golayer.app.config.EventBroker.Companion.COMMANDS_TOPIC
import io.golayer.app.config.EventBroker.Companion.SPREADSHEET_SHARE_EVENTS_TOPIC
import io.golayer.app.config.EventConsumer
import io.golayer.app.config.EventProducer
import io.golayer.app.config.StorageConfig
import io.golayer.app.config.produceEvent
import io.golayer.app.domain.Command
import io.golayer.app.domain.CreatedSharedRecord
import io.golayer.app.domain.RequestShare
import io.golayer.app.domain.repository.SpreadsheetShareRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.elasticsearch.action.index.IndexRequest
import java.time.Duration

/*
 * Simple Kafka consumers wrapped on kotlin co-routines
 * - ShareEventsConsumer
 */
class ShareEventsConsumer(private val repository: SpreadsheetShareRepository,
                          private val consumer: EventConsumer,
                          private val mapper: ObjectMapper) {
    init {
        GlobalScope.launch {
            consumer()
        }
    }

    /*
     * sheet-sharing request persistence
     * this simple kafka subscription allows back pressured processing of the share commands
     * auto-subscribing in the end of topic
     */
    private fun CoroutineScope.consumer() {
        launch {
            val running = true
            with(consumer) {
                subscribe(listOf(SPREADSHEET_SHARE_EVENTS_TOPIC))
                while (running) for (record in consumer.poll(Duration.ofMillis(100))) {
                    val share: CreatedSharedRecord = mapper.readValue(record.value())
                    repository.persist(mapper.writeValueAsString(share))

                }
                close()
            }
        }
    }
}

/*
 * Simple Kafka consumers wrapped on kotlin co-routines
 * - CommandConsumer
 */
class CommandConsumer(private val consumer: EventConsumer,
                      private val producer: EventProducer,
                      private val repository: SpreadsheetShareRepository,
                      private val mapper: ObjectMapper) {
    init {
        GlobalScope.launch {
            consumer()
        }
    }

    /*
     * sheet-sharing request persistence
     * this simple kafka subscription allows back pressured processing of the share commands
     * auto-subscribing in the end of topic
     */
    private fun CoroutineScope.consumer() {
        launch {
            val running = true
            with(consumer) {
                subscribe(listOf(COMMANDS_TOPIC))
                while (running) for (record in consumer.poll(Duration.ofMillis(100))) {
                    val command: Command<RequestShare> = mapper.readValue(record.value())

                    //persist command/aggregate
                    repository.persist(mapper.writeValueAsString(command), IndexRequest(StorageConfig.COMMANDS_INDEX))

                    //produces underlined correlated events
                    command.commands.flatMap { share ->
                        share.sections.flatMap { section ->
                            section.emails.map { CreatedSharedRecord(share.userId, section.element, it) }
                        }
                    }.also { producer.produceEvent(it) }
                }
                close()
            }
        }
    }
}


/*
 * Simple Kafka consumers wrapped on kotlin co-routines
 * - SpreadsheetCreationConsumer
 */
class SpreadsheetCreationConsumer(private val repository: SpreadsheetShareRepository, private val consumer: EventConsumer) {
    init {
        GlobalScope.launch {
            consumer()
        }
    }

    /*
     * to be done: further Spreadsheet Creation
     * this simple kafka subscription allows back pressured processing of the share commands
     * auto-subscribing in the end of topic
     */
    private fun CoroutineScope.consumer() {
        launch {
            val commandSpreadsheetCreation = """{"producesCommand":{ "action":"expensive spreadsheet creation triggered"}}"""
            val running = true
            with(consumer) {
                subscribe(listOf(SPREADSHEET_SHARE_EVENTS_TOPIC))
                while (running) for (record in consumer.poll(Duration.ofMillis(100))) {
                    repository.persist(commandSpreadsheetCreation, IndexRequest("shared_spreadsheet_created"))
                }
                close()
            }
        }
    }
}
