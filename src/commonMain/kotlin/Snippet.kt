import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Snippet(
  val id: String,
  var title: String,
  var content: String,
  var tags: List<String>,
  var timestamp: Instant
) {
  override fun toString(): String {
    return "Snippet(id='$id', title='$title', content='${content.substring(0..10)}...', tags=$tags, timestamp=$timestamp)"
  }
}

@Serializable
data class SnippetOverview(
  val id: String,
  val title: String
)