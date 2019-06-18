package io.golayer.app.web

import io.golayer.app.domain.subscriber.CommandConsumer
import io.golayer.app.domain.subscriber.ShareEventsConsumer
import io.golayer.app.domain.subscriber.SpreadsheetCreationConsumer
import io.golayer.app.web.controllers.SheetShareController
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import org.koin.standalone.KoinComponent

class Router(private val sheetController: SheetShareController,
             private val eventsConsumer: ShareEventsConsumer,
             private val creationConsumer: SpreadsheetCreationConsumer,
             private val commandsConsumer: CommandConsumer) : KoinComponent {

    fun register(app: Javalin) {
        app.routes {
            path("sheet/") {
                post(sheetController::share)
                get(sheetController::findAll)
                get(":emails", sheetController::findByEmail)
            }
        }
    }
}
