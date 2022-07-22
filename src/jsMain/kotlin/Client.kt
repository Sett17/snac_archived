import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.json.Json
import org.w3c.dom.*


val client = HttpClient(Js) {
  install(ContentNegotiation) {
    this.json(Json {
      isLenient = true
    })
  }
  install(Auth) {
    bearer {
      this.loadTokens {
        BearerTokens(cookies["token"] ?: "", "")
      }
    }
  }
}

fun main() {
  if (cookies["token"].isNullOrEmpty()) {
    window.location.href = "/login"
  }
  Editor.highlight()
  Editor.new()
  (document.querySelector("#tags span button:nth-child(3)") as HTMLButtonElement).onclick = {
    Editor.new()
  }
  (document.querySelector("#tags span button:nth-child(2)") as HTMLButtonElement).onclick = {
    updateSidebar()
  }
  document.onkeydown = {
    if (it.key == "s" && it.ctrlKey) {
      Editor.save()
      it.preventDefault()
    }
    true
  }

  window.onbeforeunload = {
    if (Editor.unsaved) "You have unsaved changes. Are you sure you want to leave?" else null
  }
  updateSidebar()
}

// completely redo
//   componentize each part
//   update only needed parts
//   fragment doms...
fun updateSidebar() {
  CoroutineScope(Dispatchers.Main).launch {
    with(Backend.tags()) {
      document.querySelectorAll(".tag, #tags input[type=radio]").asList().forEach { it.asDynamic().remove() }
      this.forEach {
        document.querySelector("#tags > div")!!.append {
          input {
            type = InputType.radio
            name = "tag"
            id = "tag-${it.name}"
          }
          div {
            classes = setOf("tag")
            attributes["data-tag"] = it.name
            label {
              htmlFor = "tag-${it.name}"
              span {
                +"${it.name} (${it.count})"
                span {
                  +"祈"
                }
              }
              onClickFunction = {
                with(it.currentTarget.asDynamic().parentElement.unsafeCast<HTMLDivElement>()) {
                  val th = this
                  CoroutineScope(Dispatchers.Main).launch {
                    th.querySelector("span span")?.textContent = "祉"
                    Backend.snippets(attributes["data-tag"]?.nodeValue!!)
                      .also { th.querySelector("ul")?.innerHTML = "" }.forEach {
                        th.querySelector("ul")?.append {
                          li {
                            span {
                              +it.id
                            }
                            +it.title
                            attributes["data-id"] = it.id
                            onClickFunction = {
                              with(it.currentTarget.unsafeCast<HTMLLIElement>()) {
                                CoroutineScope(Dispatchers.Main).launch {
                                  Editor.openSnippet(Backend.snipppet(attributes["data-id"]?.nodeValue!!))
                                }
                              }
                            }
                          }
                        }
                      }
                  }
                }
              }
            }
            ul {
            }
          }
        }
      }
    }
  }
}

//fun updateSidebar() {
//  val openTags =
//    document.querySelectorAll(".tag.open").asList().map { (it as HTMLDivElement).attributes["data-tag"]!!.value }
//  CoroutineScope(Dispatchers.Main).launch {
//    with(Backend.tags()) {
//      document.querySelectorAll("#tags > div > div").asList().forEach { it.asDynamic().remove() }
//      this.forEach {
//        (document.querySelector("#tags > div") as HTMLDivElement).append {
//          div {
//            classes = setOf("tag")
//            attributes["data-tag"] = it.name
//            span {
//              +"${it.name} (${it.count})"
//              span {
//                +"祈"
//              }
//              onClickFunction = {
//                with(it.currentTarget.asDynamic().parentElement.unsafeCast<HTMLDivElement>()) {
//                  classList.toggle("open")
//                  val th = this
//                  if (classList.contains("open")) {
//                    CoroutineScope(Dispatchers.Main).launch {
//                      th.querySelector("span span")?.textContent = "祉"
//                      Backend.snippets(attributes["data-tag"]?.nodeValue!!)
//                        .also { th.querySelector("ul")?.innerHTML = "" }.forEach {
//                          th.querySelector("ul")?.append {
//                            li {
//                              span {
//                                +it.id
//                              }
//                              +it.title
//                              attributes["data-id"] = it.id
//                              onClickFunction = {
//                                with(it.currentTarget.unsafeCast<HTMLLIElement>()) {
//                                  CoroutineScope(Dispatchers.Main).launch {
//                                    Editor.openSnippet(Backend.snipppet(attributes["data-id"]?.nodeValue!!))
//                                  }
//                                }
//                              }
//                            }
//                          }
//                        }
//                    }
//                  } else {
//                    th.querySelector("span span")?.textContent = "祈"
//                  }
//                }
//              }
//            }
//            ul {
//
//            }
//          }
//        }
//      }
//    }
//    document.querySelectorAll(".tag").asList().forEach {
//      if (openTags.contains((it as HTMLDivElement).attributes["data-tag"]!!.value)) {
//        (it.querySelector("span:first-of-type") as HTMLSpanElement).click()
//      }
//    }
//  }
//}
