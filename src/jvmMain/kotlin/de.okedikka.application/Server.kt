package de.okedikka.application

import Snippet
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.sksamuel.hoplite.ConfigLoader
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.HTML
import java.nio.file.Path
import java.util.*
import kotlin.io.path.absolutePathString

lateinit var config: Config

fun main(args: Array<String>) {
  val configPath = try {
    Path.of(args[0])
  } catch (e: Exception) {
    println("Please provide a path to the config file")
    return
  }
  config = ConfigLoader().loadConfigOrThrow(configPath.absolutePathString())
  embeddedServer(CIO, port = config.snac.port, host = "0.0.0.0") {
    installPlugins()
    routing {
      post("/login") {
        val form = call.receiveParameters()
        val password = form["password"] ?: return@post call.respond(HttpStatusCode.BadRequest)
        if (password != config.snac.password) {
          application.log.debug("Received wrong password")
          return@post call.respond(HttpStatusCode.Unauthorized)
        }
        val token = JWT.create()
          .withClaim("password", password)
          .withExpiresAt(Date(System.currentTimeMillis() + 60000 * 60 * 24))
          .sign(Algorithm.HMAC256(config.snac.secret))
        call.response.header(HttpHeaders.SetCookie, "token=$token; path=/; Expires=${Date(System.currentTimeMillis() + 60000 * 60 * 24)}")
        application.log.debug("Received correct password\tRedirecting")
        call.response.status(HttpStatusCode.TemporaryRedirect)
        call.respondRedirect("/?", false)
      }

      authenticate("jwt-cookie") {
        get("/api/all") {
          call.respond(DB.all)
        }
        get("/api/snippet/{id}") {
          with(DB.getSnippet(call.parameters["id"]!!)) {
            if (this == null) return@get call.respond(HttpStatusCode.NotFound)
            call.respond(this)
          }
        }
        post("/api/snippet/{id}") {
          val snippet = call.receive<Snippet>()
          with(DB.updateSnippet(snippet)) {
            if (this == null) {
              call.respond(HttpStatusCode(500, "Not saved"))
            }
            call.respond(this!!)
          }

        }
        delete("/api/snippet/{id}") {
          try {
            DB.deleteSnippet(call.parameters["id"]!!)
            call.respondText("Snippet deleted")
          } catch (e: Exception) {
            application.environment.log.error(e.stackTraceToString())
            call.respond(HttpStatusCode(500, "Error ${e.message}"))
          }
        }
        post("/api/new") {
          val snippet = call.receive<Snippet>()
          with(DB.newSnippet(snippet)) {
            if (this == null) {
              call.respond(HttpStatusCode(500, "Not created"))
            }
            call.respond(this!!)
          }
        }
        get("/api/tag/{tag}") {
          call.respond(DB.getTag(call.parameters["tag"]!!))
        }
        get("/api/tags") {
          call.respond(DB.tags)
        }
        get("/") {
          call.respondHtml(HttpStatusCode.OK, HTML::index)
        }
      }

      get("/login") {
        call.respondHtml(HttpStatusCode.OK, HTML::login)
      }
      static("/") {
        resources()
      }
    }
  }.start(wait = true)
}