import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.html.dom.append
import kotlinx.html.js.li
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.HTMLUListElement

object Search {
  private val inputElement: HTMLInputElement
    get() {
      return document.querySelector("#tag-search-input") as HTMLInputElement
    }
  private val resultContainer: HTMLUListElement
    get() {
      return document.querySelector("#search > ul") as HTMLUListElement
    }
  private val resetBtn: HTMLSpanElement
    get() {
      return document.querySelector("#search > div > span") as HTMLSpanElement
    }

  init {
    resetBtn.onclick = {
      inputElement.value = ""
      resultContainer.innerHTML = ""
      true
    }
    inputElement.onkeyup = {
      val value = inputElement.value.trim()
      if (value.length >= 2) {
        CoroutineScope(Dispatchers.Main).launch {
          val frag = document.createDocumentFragment()
          with(Backend.search(value)) {
            if (isNotEmpty()) {
              forEach {
                frag.append {
                  snippet(it)
                }
              }
              resultContainer.innerHTML = ""
              inputElement.style.color = "var(--green)"
              resultContainer.append(frag)
            } else {
              resultContainer.innerHTML = ""
              inputElement.style.color = "var(--red)"
              resultContainer.append {
                li {
                  +"Nothing found"
                }
              }
            }
          }
        }
      } else {
        resultContainer.innerHTML = ""
        inputElement.style.color = "inherit"
      }
    }
  }
}

