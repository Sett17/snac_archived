import * as Cursor from '/static/js/cursor.js';
import * as Backend from '/static/js/backend.js'
import * as Toast from '/static/js/toast.js'
import * as Misc from '/static/js/misc.js'

let titleInput = document.querySelector('#editor-wrapper div:first-of-type input')
let descInput = document.querySelector('#editor-wrapper > div:nth-child(2) input')
let delBtn = document.querySelector("#editor-wrapper div:first-of-type button:first-of-type")
let saveBtn = document.querySelector("#editor-wrapper div:first-of-type button:last-of-type")
let tagsWrapper = document.querySelector("#tags-info div:first-of-type")
let idField = document.querySelector("#tags-info div:last-of-type span:first-of-type")
let timestampField = document.querySelector("#tags-info div:last-of-type span:last-of-type")
let codeField = document.querySelector("#codeField")
export let unsaved = false
let originalSnippet = null
let currentSnippet = null
let highlightTimeout = null

export function init() {
    titleInput.focus()
    document.querySelector("#tags-info > div:nth-child(1) > input[type=text]").onkeypress = (e) => {
        let target = e.target
        if (e.key === 'Enter' && target.value.length > 0) {
            addTag(target.value.toUpperCase())
            target.value = ''
        }
    }
    titleInput.onkeyup = (e) => {
        currentSnippet.title = titleInput.value
        if (e.key === 'Enter') {
            save()
        }
        checkChange()
    }
    descInput.onkeyup = () => {
        currentSnippet.description = descInput.value
        checkChange()
    }
    saveBtn.onclick = () => {
        save()
    }
    delBtn.onclick = () => {
        Backend.deleteSnippet(currentSnippet.id).then(b => {
            if (b) {
                Toast.show('Snippet deleted')
                newEmpty()
                Misc.updateSidebar(false)
            }
        })
    }
    codeField.focusout = () => {
        highlight()
    }
    codeField.onkeydown = (e) => {
        if (e.key === 'Tab' && !e.shiftKey) {
            document.execCommand('insertHTML', false, '  ')
            e.preventDefault()
        }
        if (e.key === 'Enter' && e.ctrlKey) {
            highlight()
            e.preventDefault()
        } else {
            if (highlightTimeout != null) {
                window.clearTimeout(highlightTimeout)
            }
            highlightTimeout = window.setTimeout(() => {
                highlight()
            }, 500)
        }
    }
    document.querySelector("#tags-info > div:nth-child(2) > button").onclick = () => {
        if (navigator.clipboard) {
            navigator.clipboard.writeText(stringPerElement(codeField))
            Toast.show('Copied to clipboard')
        } else {
            Toast.show('Writing to clipboard not supported')
        }
    }
}

export function newEmpty() {
    openSnippet({
        id: '_____',
        title: '',
        description: '',
        content: '',
        tags: [],
        timestamp: new Date(0)
    })
}

export function highlight() {
    let curPos = Cursor.getCurrentCursorPosition(codeField)
    codeField.className = 'hljs'
    hljs.highlightAll()
    Cursor.setCurrentCursorPosition(curPos, codeField)
    updateSnippet()
    checkChange()
}

export function updateSnippet() {
    currentSnippet.content = stringPerElement(document.querySelector("#code pre"))
    currentSnippet.title = titleInput.value
    currentSnippet.description = descInput.value
}

export function stringPerElement(el) {
    let result = ''
    el.childNodes.forEach(child => {
        if (child.hasChildNodes()) {
            result += stringPerElement(child)
        } else {
            result += child.textContent
        }
    })
    return result
}

export function save() {
    if (currentSnippet.tags.length === 0) {
        Toast.show("At least one tag is required")
        return
    }
    updateSnippet()
    if (currentSnippet.id === '_____') {
        Backend.newSnippet(currentSnippet).then(snippet => {
            try {
                Misc.unrollTag(document.querySelector(`#tag-${snippet.tags[0]}`).nextSibling)
            } catch (e) {
            }
            openSnippet(snippet)
        })
    } else {
        Backend.updateSnippet(currentSnippet).then(snippet => {
            if (document.querySelector('#tags input:checked')?.id === 'no-tag') {
                try {
                    Misc.unrollTag(document.querySelector(`#tag-${snippet.tags[0]}`).nextSibling)
                } catch (e) {
                }
            }
            if (currentSnippet.tags.length !== originalSnippet.tags.length) { //no quite right but a few false positives are fine
                Misc.updateSidebar(false)
            }
            openSnippet(snippet)
        })
    }
}

export function openSnippet(snippet) {
    currentSnippet = Object.assign({}, snippet)
    originalSnippet = Object.assign({}, snippet)
    titleInput.value = currentSnippet.title
    descInput.value = currentSnippet.description
    delBtn.disabled = false
    tagsWrapper.querySelector('input').disabled = false
    displayTags()
    idField.textContent = snippet.id
    let t = new Date(snippet.timestamp)
    t = t.toLocaleString('de-DE', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    })
    timestampField.textContent = t.replace(/(?<=\.) |,/g, '') //replace weird spaces after dots and the comma
    codeField.textContent = snippet.content
    highlight()
}

export function checkChange() {
    saveBtn.disabled = currentSnippet.tags.length === 0
    if (currentSnippet.id !== originalSnippet.id ||
        currentSnippet.title !== originalSnippet.title ||
        currentSnippet.description !== originalSnippet.description ||
        currentSnippet.content !== originalSnippet.content ||
        currentSnippet.tags !== originalSnippet.tags) {
        unsaved = true
        saveBtn.setAttribute('highlight', '')
    } else {
        unsaved = false
        saveBtn.removeAttribute('highlight')
    }
}

export function displayTags() {
    tagsWrapper.querySelectorAll('span').forEach(span => {
        span.remove()
    })
    currentSnippet.tags.forEach(tag => {
        let span = document.createElement('span')
        span.textContent = tag
        span.onclick = () => {
            removeTag(tag)
        }
        tagsWrapper.appendChild(span)
    })
}

export function addTag(tag) {
    currentSnippet.tags.push(tag)
    displayTags()
    checkChange()
}

export function removeTag(tag) {
    currentSnippet.tags = currentSnippet.tags.filter(t => t !== tag)
    displayTags()
    checkChange()
}
