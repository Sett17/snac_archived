import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object Backend {
  private val auth: HttpRequestBuilder.() -> Unit = {
    headers {
      append("authorization", "Bearer ${cookies["token"]}")
    }
  }

  suspend fun tags(): Array<TagOverview> {
    return Json.decodeFromString(client.get("/api/tags").bodyAsText())
  }

  suspend fun snippets(tag: String): Array<SnippetOverview> {
    return Json.decodeFromString(client.get("/api/tag/$tag").bodyAsText())
  }

  suspend fun snipppet(id: String): Snippet {
    return Json.decodeFromString(client.get("/api/snippet/$id").bodyAsText())
  }
}