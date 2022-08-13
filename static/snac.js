const Toast = {
    toastTimeout: null,
    el: document.getElementById('toast'),
    show: function (text, duration = 3500) {
        if (this.toastTimeout != null) {
            window.clearTimeout(this.toastTimeout)
            this.close()
        }
        this.toastTimeout = window.setTimeout(() => {
            this.close()
        }, duration)
        this.el.textContent = text
        this.el.style.opacity = '1'
    },
    close: function () {
        this.el.style.opacity = '0'
        this.toastTimeout = null
    }
}
const Editor = {
    titleInput: document.querySelector('#editor-wrapper div:first-of-type input'),
    descInput: document.querySelector('#editor-wrapper > div:nth-child(2) input'),
    delBtn: document.querySelector("#editor-wrapper div:first-of-type button:first-of-type"),
    saveBtn: document.querySelector("#editor-wrapper div:first-of-type button:last-of-type"),
    tagsWrapper: document.querySelector("#tags-info div:first-of-type"),
    idField: document.querySelector("#tags-info div:last-of-type span:first-of-type"),
    timestampField: document.querySelector("#tags-info div:last-of-type span:last-of-type"),
    codeField: document.querySelector("#code pre code"),
    unsaved: false,
    originalSnippet: null,
    currentSnippet: null,
    highlightTimeout: null,
    init: function () {
        this.titleInput.focus()
        document.querySelector("#tags-info > div:nth-child(1) > input[type=text]").onkeypress = (e) => {
            let target = e.target
            if (e.key === 'Enter' && target.value.length > 0) {
                this.addTag(target.value.toUpperCase())
                target.value = ''
            }
        }
        this.titleInput.onkeyup = (e) => {
            this.currentSnippet.title = this.titleInput.value
            if (e.key === 'Enter') {
                this.save()
            }
            this.checkChange()
        }
        this.descInput.onkeyup = (e) => {
            this.currentSnippet.description = this.descInput.value
            this.checkChange()
        }
        this.saveBtn.onclick = () => {
            this.save()
        }
        this.delBtn.onclick = () => {
            Backend.deleteSnippet(this.currentSnippet.id).then(b => {
                if (b) {
                    Toast.show('Snippet deleted')
                    this.new()
                    updateSidebar(false)
                }
            })
        }
        this.codeField.focusout = () => {
            this.highlight()
        }
        this.codeField.onkeydown = (e) => {
            if (e.key === 'Tab' && !e.shiftKey) {
                document.execCommand('insertHTML', false, '  ')
                e.preventDefault()
            }
            if (e.key === 'Enter' && e.ctrlKey) {
                document.execCommand('insertHTML', false, '\n')
                e.preventDefault()
            }
            if (this.highlightTimeout != null) {
                window.clearTimeout(this.highlightTimeout)
            }
            this.highlightTimeout = window.setTimeout(() => {
                this.highlight()
            }, 3000)
        }
    },
    new: function () {
        this.openSnippet({
            id: '_____',
            title: '',
            description: '',
            content: '',
            tags: [],
            timestamp: new Date(0)
        })
    },
    highlight: function () {
        this.codeField.className = 'hljs'
        hljs.highlightAll()
        this.updateSnippet()
        this.checkChange()
    },
    updateSnippet: function () {
        this.currentSnippet.content = this.stringPerElement(document.querySelector("#code pre"))
        this.currentSnippet.title = this.titleInput.value
        this.currentSnippet.description = this.descInput.value
    },
    stringPerElement: function (el) {
        let result = ''
        el.childNodes.forEach(child => {
            if (child.hasChildNodes()) {
                result += this.stringPerElement(child)
            } else {
                result += child.textContent
            }
        })
        return result
    },
    save: function () {
        if (this.currentSnippet.tags.length === 0) {
            Toast.show("At least one tag is required")
            return
        }
        this.updateSnippet()
        if (this.currentSnippet.id === '_____') {
            Backend.newSnippet(this.currentSnippet).then(snippet => {
                try {
                    unrollTag(document.querySelector(`#tag-${snippet.tags[0]}`).nextSibling)
                } catch (e) {
                }
                this.openSnippet(snippet)
            })
        } else {
            Backend.updateSnippet(this.currentSnippet).then(snippet => {
                if (document.querySelector('#tags input:checked')?.id === 'no-tag') {
                    try {
                        unrollTag(document.querySelector(`#tag-${snippet.tags[0]}`).nextSibling)
                    } catch (e) {
                    }
                }
                if (this.currentSnippet.tags.length !== this.originalSnippet.tags.length) { //no quite right but a few false positives are fine
                    updateSidebar(false)
                }
                this.openSnippet(snippet)
            })
        }
    },
    openRemoteSnippet: async function (id) {
        this.openSnippet(await fetch(`/api/snippet/${id}`).then(res => res.json()))
    },
    openSnippet: function (snippet) {
        this.currentSnippet = Object.assign({}, snippet)
        this.originalSnippet = Object.assign({}, snippet)
        this.titleInput.value = this.currentSnippet.title
        this.descInput.value = this.currentSnippet.description
        this.delBtn.disabled = false
        this.tagsWrapper.querySelector('input').disabled = false
        this.displayTags()
        this.idField.textContent = snippet.id
        let t = new Date(snippet.timestamp)
        t = t.toLocaleString('de-DE', {
            day: '2-digit',
            month: 'short',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        })
        this.timestampField.textContent = t.replace(/(?<=\.) |,/g, '') //replace weird spaces after dots and the comma
        this.codeField.textContent = snippet.content
        this.highlight()
    },
    checkChange: function () {
        this.saveBtn.disabled = this.currentSnippet.tags.length === 0
        if (this.currentSnippet.id !== this.originalSnippet.id ||
            this.currentSnippet.title !== this.originalSnippet.title ||
            this.currentSnippet.description !== this.originalSnippet.description ||
            this.currentSnippet.content !== this.originalSnippet.content ||
            this.currentSnippet.tags !== this.originalSnippet.tags) {
            this.unsaved = true
            this.saveBtn.setAttribute('highlight', '')
        } else {
            this.unsaved = false
            this.saveBtn.removeAttribute('highlight')
        }
    },
    displayTags: function () {
        this.tagsWrapper.querySelectorAll('span').forEach(span => {
            span.remove()
        })
        this.currentSnippet.tags.forEach(tag => {
            let span = document.createElement('span')
            span.textContent = tag
            span.onclick = () => {
                this.removeTag(tag)
            }
            this.tagsWrapper.appendChild(span)
        })
    },
    addTag: function (tag) {
        this.currentSnippet.tags.push(tag)
        this.displayTags()
        this.checkChange()
    },
    removeTag: function (tag) {
        this.currentSnippet.tags = this.currentSnippet.tags.filter(t => t !== tag)
        this.displayTags()
        this.checkChange()
    }
}

document.querySelector("#tags span button:nth-child(3)").onclick = () => {
    Editor.new()
}
document.querySelector("#tags span button:nth-child(2)").onclick = () => {
    updateSidebar()
}
document.onkeydown = (e) => {
    if (e.key === 's' && e.ctrlKey) {
        Editor.save()
        e.preventDefault()
    }
}
window.onfocus = async () => {
    checker()
}
window.onbeforeunload = () => {
    if (Editor.unsaved === true) {
        return 'You have unsaved changes. Are you sure you want to leave?'
    } else {
        return null
    }
}
updateSidebar(false)

async function checker() {
    if (!await fetch('/authorized').then(res => res.text()).then(text => text === 'true')) window.location.href = '/login'
}

async function updateSidebar(showToast = true) {
    document.querySelector('#tags > div').innerHTML =
        await (await fetch('/render/tags')).text();
    if (showToast) {
        Toast.show('Tags updated')
    }
}

async function unrollTag(el) {
    el.querySelector('ul').innerHTML =
        await (await fetch('/render/tag/' + el.dataset.tag)).text();
}