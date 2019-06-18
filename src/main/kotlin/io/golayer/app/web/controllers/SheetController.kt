package io.golayer.app.web.controllers

import io.golayer.app.domain.Command
import io.golayer.app.domain.SharedCreatedRecordsDTO
import io.golayer.app.domain.RequestShare
import io.golayer.app.domain.ShareRequestDTO
import io.golayer.app.domain.service.SheetService
import io.golayer.app.utils.isEmailValid
import io.golayer.app.utils.isSharedElementValid
import io.javalin.Context
import java.util.*

class SheetController(private val service: SheetService) {

    fun share(ctx: Context) {
        ctx.validatedBody<ShareRequestDTO>()
                .check({ emailPattern(it.commands) }, "invalid emails")
                .check({ spreadSheetValidation(it.commands) }, "invalid shared element")
                .getOrThrow().apply {
                    UUID.randomUUID().also { uuid ->
                        with(Command(id = uuid.toString(), commands = this.commands)) {
                            service.process(this)
                        }
                        ctx.result(uuid.toString())
                    }
                }
    }

    fun findByEmail(ctx: Context) {
        ctx.validatedPathParam("emails")
                .check({ it.isEmailValid() })
                .getOrThrow().apply {
                    service.findByEmail(email = this).also { records ->
                        ctx.json(SharedCreatedRecordsDTO(records))
                    }
                }
    }

    fun findAll(ctx: Context) {
        ctx.apply { service.findAll().also { records -> ctx.json(SharedCreatedRecordsDTO(records)) } }
    }

    private fun emailPattern(list: List<RequestShare>): Boolean =
            list.flatMap { it.sections }.flatMap { it.emails }.all { it.isEmailValid() }

    private fun spreadSheetValidation(list: List<RequestShare>): Boolean =
            list.flatMap { it.sections }.map { it.element }
                    .all { it.isSharedElementValid() && service.validateSheet(it) }

}