import * as Editor from '/js/editor.js'
import * as Search from '/js/search.js'
import * as Misc from '/js/misc.js'


Misc.checker()

hljs.configure({
    ignoreUnescapedHTML: true
})

Search.init()
Editor.init()
Editor.newEmpty()
document.querySelector("#tags span button:nth-child(3)").onclick = () => {
    Editor.newEmpty()
}
document.querySelector("#tags span button:nth-child(2)").onclick = () => {
    Misc.updateSidebar()
}
document.onkeydown = (e) => {
    if (e.key === 's' && e.ctrlKey) {
        Editor.save()
        e.preventDefault()
    }
}
window.onfocus = async () => {
    Misc.checker()
}
window.onbeforeunload = () => {
    if (Editor.unsaved === true) {
        return 'You have unsaved changes. Are you sure you want to leave?'
    } else {
        return null
    }
}
Misc.updateSidebar(false)
