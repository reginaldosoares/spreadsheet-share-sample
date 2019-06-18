package io.golayer.app.config

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.golayer.app.config.ModulesConfig.allModules
import io.golayer.app.web.ErrorExceptionMapping
import io.golayer.app.web.Router
import io.javalin.Javalin
import io.javalin.JavalinEvent
import io.javalin.json.JavalinJackson
import org.koin.core.KoinProperties
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext
import org.koin.standalone.getProperty
import org.koin.standalone.inject
import java.text.SimpleDateFormat


class AppConfig : KoinComponent {
    private val router: Router by inject()

    fun setup(): Javalin {
        StandAloneContext.startKoin(allModules,
                KoinProperties(useEnvironmentProperties = true, useKoinPropertiesFile = true))

        return Javalin.create()
                .also { app ->
                    objectMapper
                    app.enableCorsForAllOrigins()
                            .contextPath(getProperty("context"))
                            .event(JavalinEvent.SERVER_STOPPING) {
                                StandAloneContext.stopKoin()
                            }
                    router.register(app)
                    ErrorExceptionMapping.register(app)
                    app.port(getProperty("server_port"))
                }
    }

    companion object {
        val objectMapper =
                jacksonObjectMapper().apply {
                    JavalinJackson.configure(this
                            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                            .setDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                            .configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true)
                            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                    )
                }


    }
}

