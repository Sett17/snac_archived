package de.okedikka.application

import com.auth0.jwt.JWT
import com.codahale.metrics.Slf4jReporter
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.auth.*
import io.ktor.server.logging.*
import io.ktor.server.metrics.dropwizard.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.reflect.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import java.util.concurrent.TimeUnit

fun Application.installPlugins() {
  install(ContentNegotiation) {
    json(Json {
      isLenient = true
    })
  }
  install(StatusPages) {
    status(HttpStatusCode.NotFound) { call, status ->
      call.respondText(text = "404: Page Not Found", status = status)
    }
    status(HttpStatusCode.Unauthorized) { call, status ->
      call.respondText(text = "You can't access this", status = status)
    }
    exception<Throwable> { call, cause ->
      call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
    }
  }
  install(Compression) {
    gzip()
    deflate()
  }
//  install(CallLogging) {
//    level = Level.INFO
////    filter { call ->
////      call.request.path().startsWith("/api/v1")
////    }
//    format { call ->
//      "\t${call.request.httpMethod.value}\t${call.request.path()}\t${call.request.}"
//    }
//  }
  install(DropwizardMetrics) {
    registerJvmMetricSets = false
    baseName = "snac"
    Slf4jReporter.forRegistry(registry)
      .outputTo(this@installPlugins.log)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .filter { name, _ -> name.startsWith("snac./") }
      .build()
      .start(20, TimeUnit.SECONDS)
  }
  install(CachingHeaders) {
    options { _, outgoingContent ->
      when (outgoingContent.contentType?.withoutParameters()) {
        ContentType.Application.Json -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 2))
        ContentType(
          "application",
          "x-font-ttf"
        ) -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 60 * 60 * 24 * 365))

        ContentType.Text.Any -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 60 * 60 * 24))
        ContentType.Application.Any -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 60 * 60 * 24))
        else -> null
      }
    }
  }
  install(Authentication) {
    provider("jwt-cookie") {
      this.authenticate {
        val token = it.call.request.cookies["token"]
        if (token.isNullOrEmpty() || JWT.decode(token).getClaim("password").asString() != config.snac.password) {
          return@authenticate it.challenge("jwt-cookie", AuthenticationFailedCause.InvalidCredentials) { _, call ->
            call.respondRedirect("/login")
          }
        }
      }
    }
  }

}
