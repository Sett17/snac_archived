import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.span
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.w3c.dom.*
import org.w3c.dom.events.KeyboardEvent

object Editor {
  private val titleInput: HTMLInputElement
    get() {
      return document.querySelector("#editor-wrapper div:first-of-type input") as HTMLInputElement
    }
  private val descInput: HTMLInputElement
    get() {
      return document.querySelector("#editor-wrapper > div:nth-child(2) input") as HTMLInputElement
    }
  private val delBtn: HTMLButtonElement
    get() {
      return document.querySelector("#editor-wrapper div:first-of-type button:first-of-type") as HTMLButtonElement
    }
  private val saveBtn: HTMLButtonElement
    get() {
      return document.querySelector("#editor-wrapper div:first-of-type button:last-of-type") as HTMLButtonElement
    }
  private val tagsWrapper: HTMLDivElement
    get() {
      return document.querySelector("#tags-info div:first-of-type") as HTMLDivElement
    }
  private val idField: HTMLSpanElement
    get() {
      return document.querySelector("#tags-info div:last-of-type span:first-of-type") as HTMLSpanElement
    }
  private val timestampField: HTMLSpanElement
    get() {
      return document.querySelector("#tags-info div:last-of-type span:last-of-type") as HTMLSpanElement
    }
  private val codeField: HTMLElement
    get() {
      return document.querySelector("#code pre code") as HTMLElement
    }

  var unsaved = false
    private set
  private var originalSnippet: Snippet? = null
  private var currentSnippet: Snippet? = null

  init {
    titleInput.focus()
    document.querySelector("#tags-info > div:nth-child(1) > input[type=text]")!!.addEventListener("keypress", {
      it as KeyboardEvent
      with(it.target as HTMLInputElement) {
        if (it.key == "Enter" && this.value.isNotEmpty()) {
          addTag(value.toUpperCasePreservingASCIIRules())
          value = ""
        }
      }
    })
    titleInput.onkeypress = {
      currentSnippet?.title = titleInput.value
      if (it.key == "Enter") {
        save()
      }
      checkChange()
    }
    descInput.onkeypress = {
      currentSnippet?.description = descInput.value
      checkChange()
    }
    saveBtn.onclick = {
      save()
    }
    delBtn.onclick = {
      CoroutineScope(Dispatchers.Main).launch {
        val response = client.delete("/api/snippet/${currentSnippet?.id}")
        Toast(response.bodyAsText())
        if (response.status == HttpStatusCode.OK) {
          unsaved = false
          originalSnippet = currentSnippet
          checkChange()
          updateSidebar()
          new()
        }
      }
    }
    with(codeField) {
      addEventListener("focusout", {
        highlight()
      })
      onkeydown = {
        if (it.key == "Tab" && !it.shiftKey) {
          document.execCommand("insertHTML", false, "  ")
          it.preventDefault()
        }
        if (it.key == "Enter" && it.ctrlKey) {
          highlight()
          it.preventDefault()
        } else if (it.key == "Enter") {
          document.execCommand("insertHTML", false, "\n")
          it.preventDefault()
        }
        highlightTimeout?.let { window.clearTimeout(it) }
        highlightTimeout = window.setTimeout({
          highlight()
        }, 3000)
        true
      }
    }
  }

  fun new() {
    openSnippet(
      Snippet(
        id = "_____", title = "", description = "", content = "", tags = listOf(), timestamp = Instant.fromEpochMilliseconds(0)
      )
    )
  }

  var highlightTimeout: Int? = null
  fun highlight() {
    codeField.className = "hljs"
    js("hljs.highlightAll()")
    CoroutineScope(Dispatchers.Main).launch {
      updateSnippet()
      checkChange()
    }
  }

  private fun updateSnippet() {
    currentSnippet?.content = stringPerElement(document.querySelector("#code pre")!!)
    currentSnippet?.title = titleInput.value
    currentSnippet?.description = descInput.value
  }

  private fun stringPerElement(Element: Element): String {
    val c = StringBuilder()
    Element.childNodes.asList().forEach {
      if (it.hasChildNodes()) {
        c.append(stringPerElement(it as HTMLElement))
      } else {
        c.append(it.textContent)
      }
    }
    return c.toString()
  }

  fun save() {
    if (currentSnippet?.tags?.isEmpty() == true) {
      Toast("At least one tag is required")
      return
    }
    CoroutineScope(Dispatchers.Main).launch {
      updateSnippet()
      val response: HttpResponse
      if (currentSnippet!!.id == "_____") {
        response = client.post("/api/new") {
          contentType(ContentType.Application.Json)
          setBody(Json.encodeToJsonElement(currentSnippet!!))
        }
        if (response.status != HttpStatusCode.OK) {
          Toast(response.bodyAsText())
          return@launch
        }
        Toast("Snippet created")
        updateSidebar()
        openSnippet(response.body())
      } else {
        response = client.post("/api/snippet/${currentSnippet?.id}") {
          contentType(ContentType.Application.Json)
          setBody(Json.encodeToJsonElement(currentSnippet!!))
        }
        if (response.status != HttpStatusCode.OK) {
          Toast(response.bodyAsText())
          return@launch
        }
        Toast("Snippet updated")
        openSnippet(response.body())
      }
    }
  }

  fun openSnippet(snippet: Snippet) {
    currentSnippet = snippet.copy()
    originalSnippet = snippet.copy()
    titleInput.value = snippet.title
    descInput.value = snippet.description
    titleInput.disabled = false
    delBtn.disabled = false
    (tagsWrapper.querySelector("input") as HTMLInputElement).disabled = false
    displayTags()
    idField.textContent = snippet.id
    timestampField.textContent = with(snippet.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())) {
      "${dayOfMonth.toString().padStart(2, '0')}.${month.name.first()}${
        month.name.substring(1, 3).lowercase()
      }.${year.toString().padStart(4, '0')} ${
        hour.toString().padStart(2, '0')
      }:${minute.toString().padStart(2, '0')}:${second.toString().padStart(2, '0')}"
    }
    codeField.textContent = snippet.content
    highlight()
  }

  private fun checkChange() {
    saveBtn.disabled = currentSnippet?.tags?.isEmpty() == true
    if (currentSnippet != originalSnippet) {
      unsaved = true
      saveBtn.asDynamic().setAttribute("highlight", "")
    } else {
      unsaved = false
      saveBtn.removeAttribute("highlight")
    }
  }

  private fun displayTags() {
    tagsWrapper.querySelectorAll("span").asList().forEach { it.asDynamic().remove() }
    for (tag in currentSnippet!!.tags) {
      tagsWrapper.append {
        span {
          +tag
          onClickFunction = {
            removeTag(tag)
          }
        }
      }
    }
  }

  fun addTag(tag: String) {
    currentSnippet!!.tags += tag
    displayTags()
    checkChange()
  }

  fun removeTag(tag: String) {
    currentSnippet!!.tags -= tag
    displayTags()
    checkChange()
  }

//  private fun createRange(node: Node, charsCountPar: Int, rangePar: Range?): Range? {
//    var range = rangePar
//    var charsCount = charsCountPar
//    if (range == null) {
//      range = document.createRange()
//      range.selectNode(node)
//      range.setStart(node, 0)
//    }
//    if (charsCount == 0) {
//      range.setEnd(node, 0)
//    } else {
//      if (node.nodeType == Node.TEXT_NODE) {
//        if (node.textContent!!.length < charsCount) {
//          charsCount -= node.textContent!!.length
//        } else {
//          range.setEnd(node, charsCount)
//          charsCount = 0
//        }
//      } else {
//        for (child in node.childNodes.asList()) {
//          range = createRange(child, charsCount, range)
//
//          if (charsCount == 0) {
//            break
//          }
//        }
//      }
//    }
//    return range
//  }
//
//  private fun setCursorPos(chars: Int) {
//    require(chars >= 0)
//    var selection = js("window.getSelection()")
//    var range = createRange(codeField, chars, null)
//
//    if (range != null) {
//      range.collapse(false)
//      selection.removeAllRanges()
//      selection.addRange(range)
//    }
//  }
//
//  private fun isChildOf(node)
}