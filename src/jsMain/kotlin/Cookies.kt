import kotlinx.browser.document

val cookies
  get(): Map<String, String> {
    val split = document.cookie.split("; ")
    if (split.first().isEmpty()) return mapOf("" to "")
    return split.associate { val (left, right) = it.split("="); left to right }
  }