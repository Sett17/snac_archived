package de.okedikka.application

import kotlinx.html.*

fun HTML.login() {
  head {
    title("snac - Login")
    link(rel = "stylesheet", href = "/style.css")
    link(rel = "stylesheet", href = "https://fonts.googleapis.com/icon?family=Material+Icons")
    meta(content = "text/html; charset=utf-8")
    script {
      src = "/login.js"
      defer = true
    }
  }
  body {
    div {
      id = "root"
      form {
        id = "login"
        action = "/login"
        input {
          type = InputType.password
          name = "password"
          placeholder = "Password"
        }
        input {
          type = InputType.submit
          value = "Login"
        }
      }
    }
  }
}