let toastTimeout = null
let el = document.getElementById('toast')

export function show(text, duration = 3500) {
    if (toastTimeout != null) {
        window.clearTimeout(toastTimeout)
        close()
    }
    toastTimeout = window.setTimeout(() => {
        close()
    }, duration)
    el.textContent = text
    el.style.opacity = '1'
}

export function close() {
    el.style.opacity = '0'
    toastTimeout = null
}
