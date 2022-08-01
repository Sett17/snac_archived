package de.okedikka.application

import com.github.nwillc.ksvg.elements.SVG
import kotlinx.html.*

fun HTML.index() {
  head {
    title("snac - Snippet Accumulator")
    link(rel = "stylesheet", href = "/style.css")
    link(rel = "stylesheet", href = "https://fonts.googleapis.com/icon?family=Material+Icons")
    meta(content = "text/html; charset=utf-8")
    script(src = "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.6.0/highlight.min.js") {}
    link(rel = "stylesheet", href = "/copilot.css")
  }
  body {
    div {
      id = "root"
      div {
        id = "sidebar"
        div {
          id = "logo"
          unsafe {
            raw(
              SVG.svg {
                viewBox = "0 0 70 50"
                text {
                  body = "snac"
                  fontFamily = "monospace"
                  fontSize = "20"
                  fill = "#939DA5"
                  y = "25"
                  x = "50%"
                  attributes["text-anchor"] = "middle"
                }
              }.toString()
            )
          }
        }
        div {
          id = "search"
          h3 { +"Search" }
          div {
            input {
              id = "tag-search-input"
              placeholder = "Regex search"
            }
            span {
              +"×"
            }
          }
          ul {}
        }
        div {
          id = "tags"
          label {
            htmlFor = "no-tag"
            span {
              h3 { +"Tags" }
              button { +"勒" }
              button { +"" }
            }
          }
          div {
            radioInput {
              name = "tag"
              id = "no-tag"
            }
          }
        }
      }
      div {
        id = "editor-wrapper"
        div {
          textInput {
            placeholder = "Title"
            tabIndex = "1"
          }
          button {
            +""
          }
          button {
            +""
//            disabled = true
          }
        }
        div {
          textInput {
            placeholder = "Description"
            tabIndex = "2"
          }
        }
        div {
          id = "tags-info"
          div {
            textInput {
              placeholder = "Tags seperated by <return>"
              tabIndex = "3"
            }
          }
          div {
            span {
              +""
            }
            span {
              +""
            }
          }
        }
        div {
          id = "code"
          pre {
            code {
              contentEditable = true
              spellCheck = false
              tabIndex = "4"
            }
          }
        }
      }
    }
    div {
      id = "toast"
    }
    script(src = "/snac.js") {}
    script {
      unsafe {
        raw(
          """
if ('serviceWorker' in navigator) {
  // Use the window load event to keep the page load performant
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js');
  });
}""".trimIndent()
        )
      }
    }
  }
}