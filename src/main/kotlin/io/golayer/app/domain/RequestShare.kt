package io.golayer.app.domain


import java.time.Instant
import java.util.*
import java.util.Date.from


//dto aggregator
data class ShareRequestDTO(val commands: List<RequestShare>)

data class SharedCreatedRecordsDTO(val sharedRecords: List<CreatedSharedRecord>)


//command type domain
data class RequestShare(val userId: String, val sections: List<ShareSection>) : CommandType
data class ShareSection(val element: String, val emails: List<String>)


//event type domain
data class CreatedSharedRecord(val userId: String, val element: String, val email: String) : EventType


//structural types
data class Command<out T : CommandType>(val id: String, val requestedAt: Date = from(Instant.now()), val commands: List<T>)

interface CommandType
interface EventType

