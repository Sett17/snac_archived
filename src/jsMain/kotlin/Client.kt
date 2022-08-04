import io.ktor.client.*
import io.ktor.client.engine.js.*
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
import kotlin.collections.set


val client = HttpClient(Js) {
  install(ContentNegotiation) {
    this.json(Json {
      isLenient = true
    })
  }
}

fun main() {
  Search
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
  window.onfocus = {
    CoroutineScope(Dispatchers.Main).launch {
      if (!Backend.isAuthenticated()) window.location.href = "/login"
    }
  }
//  if (js("navigator.connection.saveData") != true) {
//    window.setInterval({
//      updateSidebar(false)
//    }, 20000)
//  }
  window.onbeforeunload = {
    if (Editor.unsaved) "You have unsaved changes. Are you sure you want to leave?" else null
  }
  updateSidebar(false)
}

fun updateSidebar(showToast: Boolean = true) {
  CoroutineScope(Dispatchers.Main).launch {
    val frag = document.createDocumentFragment()
    val openRadio = document.querySelector("#tags input:checked")?.id
    with(Backend.tags()) {
      this.forEach {
        frag.append {
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
                CoroutineScope(Dispatchers.Main).launch {
                  unrollTag(it.currentTarget.asDynamic().parentElement.unsafeCast<HTMLDivElement>())
                }
              }
            }
            ul {
              this.style = "min-height: calc(${it.count} * (1em + 8px))"
            }
          }
        }
      }
      CoroutineScope(Dispatchers.Main).launch {
        if (openRadio != null) {
          unrollTag(frag.querySelector("#$openRadio + .tag") as HTMLDivElement)
        }
        document.querySelectorAll(".tag, #tags input[type=radio]").asList().forEach { it.asDynamic().remove() }
        document.querySelector("#tags > div")!!.append(frag)
        if (showToast) Toast("Tags updated")
      }
    }
  }
}

suspend fun unrollTag(el: HTMLDivElement) {
  el.asDynamic().previousSibling.checked = true
  el.querySelector("span span")?.textContent = "祉"
  Backend.snippets(el.attributes["data-tag"]?.nodeValue!!)
    .also { el.querySelector("ul")?.innerHTML = "" }.forEach {
      el.querySelector("ul")?.append {
        snippet(it)
      }
    }
}

fun TagConsumer<HTMLElement>.snippet(snippetOverview: SnippetOverview) {
  li {
    span {
      +snippetOverview.id
    }
    +snippetOverview.title
    attributes["data-id"] = snippetOverview.id
    onClickFunction = {
      with(it.currentTarget.unsafeCast<HTMLLIElement>()) {
        CoroutineScope(Dispatchers.Main).launch {
          Editor.openSnippet(Backend.snippet(attributes["data-id"]?.nodeValue!!))
        }
      }
    }
  }
}