package io.golayer.app.config

import io.golayer.app.domain.repository.SpreadsheetShareRepository
import io.golayer.app.domain.service.SheetService
import io.golayer.app.domain.subscriber.CommandConsumer
import io.golayer.app.domain.subscriber.ShareEventsConsumer
import io.golayer.app.domain.subscriber.SpreadsheetCreationConsumer
import io.golayer.app.web.Router
import io.golayer.app.web.controllers.SheetController
import org.koin.dsl.module.module

/*
 * DI modules configuration
 * using Koin
 */
object ModulesConfig {

    private val configModule = module {

        val broker = EventBroker(
                servers = getProperty("kafka.servers"),
                consumerGroup = getProperty("kafka.default.group"),
                keySe = getProperty("kafka.key.serializer"),
                ValSe = getProperty("kafka.value.serializer"),
                keyDes = getProperty("kafka.key.deserializer"),
                ValDes = getProperty("kafka.value.deserializer")
        )

        single { AppConfig() }
        single { AppConfig.objectMapper }
        single { broker.getProducer() }
        factory { broker.getConsumer() }
        single {
            StorageConfig(
                    Pair(getProperty("es.node1.host"), getProperty("es.node1.port")),
                    Pair(getProperty("es.node2.host"), getProperty("es.node2.port"))
            ).getClient()
        }
        single { Router(get(), get(), get(), get()) }
    }

    private val spreadsheetShareModule = module {
        single { SheetController(get()) }
        single { SheetService(get(), get()) }
        single { SpreadsheetShareRepository(get(), get()) }
    }

    private val brokerSubscriptionsModule = module {
        single { ShareEventsConsumer(get(), get(), get()) }
        single { SpreadsheetCreationConsumer(get(), get()) }
        single { CommandConsumer(get(), get(), get(), get()) }
    }

    internal val allModules = listOf(configModule, spreadsheetShareModule, brokerSubscriptionsModule)
}
