import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLDivElement

object Toast {
  private var toastTimeout: Int? = null
  const val short = 3500
  const val long = 6000
  private val el: HTMLDivElement get() = document.getElementById("toast") as HTMLDivElement

  operator fun invoke(message: String, duration: Int = short) {
    if (toastTimeout != null) {
      window.clearTimeout(toastTimeout!!)
      toastClose()
    }
    toastTimeout = window.setTimeout({
      toastClose()
    }, duration)
    el.run {
      this.textContent = message
      this.style.opacity = "1"
    }
  }

  private fun toastClose() {
    el.style.opacity = "0"
    toastTimeout = null
  }
}