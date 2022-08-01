import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

object Backend {

  suspend fun tags(): Array<TagOverview> {
    return Json.decodeFromString(client.get("/api/tags").bodyAsText())
  }

  suspend fun snippets(tag: String): Array<SnippetOverview> {
    return Json.decodeFromString(client.get("/api/tag/$tag").bodyAsText())
  }

  suspend fun snippet(id: String): Snippet {
    return Json.decodeFromString(client.get("/api/snippet/$id").bodyAsText())
  }

  suspend fun deleteSnippet(id: String): Boolean {
    val response = client.delete("/api/snippet/${id}")
    Toast(response.bodyAsText())
    return response.status == HttpStatusCode.OK
  }

  suspend fun updateSnippet(snippet: Snippet): Snippet? {
    val response = client.post("/api/snippet/${snippet.id}") {
      contentType(ContentType.Application.Json)
      setBody(Json.encodeToJsonElement(snippet))
    }
    return if (response.status == HttpStatusCode.OK) {
      Toast("Snippet updated")
      Json.decodeFromString(response.bodyAsText())
    } else {
      Toast(response.bodyAsText())
      null
    }
  }

  suspend fun newSnippet(snippet: Snippet): Snippet? {
    val response = client.post("/api/new") {
      contentType(ContentType.Application.Json)
      setBody(Json.encodeToJsonElement(snippet))
    }
    return if (response.status == HttpStatusCode.OK) {
      Toast("Snippet created")
      updateSidebar(false)
      Json.decodeFromString(response.bodyAsText())
    } else {
      Toast(response.bodyAsText())
      null
    }
  }
}