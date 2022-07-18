import kotlinx.serialization.Serializable

@Serializable
data class TagOverview(val name: String, val count: Int)
