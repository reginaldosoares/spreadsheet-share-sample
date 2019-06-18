package io.golayer.app.domain.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.golayer.app.config.*
import io.golayer.app.domain.Command
import io.golayer.app.domain.RequestShare
import io.golayer.app.domain.CreatedSharedRecord
import io.golayer.app.domain.repository.SpreadsheetShareRepository
import io.golayer.app.utils.memoize

class SheetService(private val repository: SpreadsheetShareRepository,
                   private val event: EventProducer) {
    /*
     *  produces async Command share event
     */
    fun process(shareCommand: Command<RequestShare>) {
        event.producesCommand(shareCommand)
    }

    /*
     *  request all share documents
     *  some restrictions applies on the sync data store http operations
     */
    fun findAll(): List<CreatedSharedRecord> = repository.findAll().getOrDefault(emptyList())

    /*
     *  return user shared elements
     *  restriction
     */
    fun findByEmail(email: String): List<CreatedSharedRecord> = repository.findByEmail(email).getOrDefault(emptyList())

    /*
     * This is a memorized wrapped function then avoiding to query a expensive resource
     * the default retention policies are inhered from the the memoize function
     */
    val validateSheet: (String) -> Boolean = { id: String ->
        repository.isSpreadsheetValid(id.split("!")[0].removeSurrounding("'"))
    }.memoize()

}