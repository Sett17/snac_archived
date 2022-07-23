package de.okedikka.application

import com.auth0.jwt.JWT
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

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
  install(CallLogging) {
    level = Level.INFO
  }
  install(CachingHeaders) {
    options { _, outgoingContent ->
      when (outgoingContent.contentType?.withoutParameters()) {
        ContentType.Application.Json -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 2))
        ContentType.Text.Any -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 60 * 60 * 5))
        else -> null
      }
    }
  }
  install(Authentication) {
    provider("jwt-cookie") {
      this.authenticate {
        val token = it.call.request.cookies["token"]
        if (token.isNullOrEmpty()) {
          return@authenticate it.challenge("jwt-cookie", AuthenticationFailedCause.NoCredentials) { _, call ->
            call.respondRedirect("/login")
          }
        }
        if (JWT.decode(token).getClaim("password").asString() != config.snac.password) {
          return@authenticate it.challenge("jwt-cookie", AuthenticationFailedCause.InvalidCredentials) { _, call ->
            call.respondRedirect("/login")
          }
        }
      }
    }
  }

}
