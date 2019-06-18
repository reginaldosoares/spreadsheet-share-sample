package io.golayer.app.web.util

import com.mashape.unirest.http.ObjectMapper
import com.mashape.unirest.http.Unirest
import io.javalin.core.util.Header
import io.javalin.json.JavalinJson
import java.time.Duration
import java.time.Duration.ofSeconds

class HttpUtil(port: Int) {
    private val json = "application/json"
    val headers = mutableMapOf(Header.ACCEPT to json, Header.CONTENT_TYPE to json)

    init {
        Unirest.setObjectMapper(object : ObjectMapper {
            override fun <T> readValue(value: String, valueType: Class<T>): T {
                return JavalinJson.fromJson(value, valueType)
            }

            override fun writeValue(value: Any): String {
                return JavalinJson.toJson(value)
            }
        })
    }

    @JvmField
    val origin: String = "http://localhost:$port"

    inline fun <reified T> post(path: String) =
            Unirest.post(origin + path).headers(headers).asObject(T::class.java)

    inline fun <reified T> post(path: String, body: Any) =
            Unirest.post(origin + path).headers(headers).body(body).asObject(T::class.java)

    inline fun <reified T> get(path: String, params: Map<String, Any>? = null) =
            Unirest.get(origin + path).headers(headers).queryString(params).asObject(T::class.java)

    inline fun <reified T> put(path: String, body: Any) =
            Unirest.put(origin + path).headers(headers).body(body).asObject(T::class.java)

    inline fun <reified T> deleteWithResponseBody(path: String) =
            Unirest.delete(origin + path).headers(headers).asObject(T::class.java)

    fun delete(path: String) =
            Unirest.delete(origin + path).headers(headers).asString()



}

internal fun <T> eventually(waitDuration: Duration = ofSeconds(1), sleepMillis: Long = 500, f: () -> T) =
        eventually(waitDuration, Throwable::class.java) {
            Thread.sleep(sleepMillis)
            f()
        }
