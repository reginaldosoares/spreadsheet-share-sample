package io.golayer.app.config

import io.golayer.app.domain.Command
import io.golayer.app.domain.EventType
import io.golayer.app.utils.concatRand
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.*

/*
 * defines Kafka as Broker configuration
 * using the official kafka provided producer/consumer java API
 */
class EventBroker(private val servers: String, private val consumerGroup: String,
                  keySe: String, ValSe: String, keyDes: String, ValDes: String) {

    private val propsProducer = Properties().apply {
        this["bootstrap.servers"] = servers
        this["key.serializer"] = keySe
        this["value.serializer"] = ValSe
    }

    private val producer = EventProducer(propsProducer)

    fun getProducer(): EventProducer = producer

    private val propsConsumer = Properties().apply {
        this["bootstrap.servers"] = servers
        this["key.deserializer"] = keyDes
        this["value.deserializer"] = ValDes
    }

    fun getConsumer(groupId: String = consumerGroup): EventConsumer =
            EventConsumer(propsConsumer.apply { this["group.id"] = groupId.concatRand() })

    companion object {
        const val SPREADSHEET_SHARE_EVENTS_TOPIC = "spreadsheet_share_events"
        const val COMMANDS_TOPIC = "commands"
    }
}

typealias EventProducer = KafkaProducer<String, String>
typealias EventRecord = ProducerRecord<String, String>
typealias EventConsumer = KafkaConsumer<String, String>

fun <T : Command<*>> EventProducer.producesCommand(entity: T) =
        this.send(EventRecord(EventBroker.COMMANDS_TOPIC, AppConfig.objectMapper.writeValueAsString(entity)))

fun <T : EventType> EventProducer.produceEvent(entity: T, topic: String = EventBroker.SPREADSHEET_SHARE_EVENTS_TOPIC) =
        this.send(EventRecord(topic, AppConfig.objectMapper.writeValueAsString(entity)))

fun <T : EventType> EventProducer.produceEvent(entity: List<T>, topic: String = EventBroker.SPREADSHEET_SHARE_EVENTS_TOPIC) =
        entity.forEach { this.send(EventRecord(topic, AppConfig.objectMapper.writeValueAsString(it))) }

