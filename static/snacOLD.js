const Backend = {
    tags: async function () {
        return await fetch('/api/tags').then(res => res.json())
    },
    byTag: async function (tag) {
        return await fetch(`/api/tag/${tag}`).then(res => res.json())
    },
    snippet: async function (id) {
        return await fetch(`/api/snippet/${id}`).then(res => res.json())
    },
    deleteSnippet: async function (id) {
        return await fetch(`/api/snippet/${id}`, {
            method: 'DELETE'
        }).then(res => res.ok)
    },
    updateSnippet: async function (snippet) {
        let res = await fetch(`/api/snippet/${snippet.id}`, {
            method: 'POST',
            body: JSON.stringify(snippet)
        })
        if (res.status === 200) {
            Toast.show('Snippet updated')
            return await res.json()
        } else {
            Toast.show(await res.text())
        }
    },
    newSnippet: async function (snippet) {
        let res = await fetch('/api/new', {
            method: 'POST',
            body: JSON.stringify(snippet)
        })
        if (res.status === 200) {
            Toast.show('Snippet created')
            updateSidebar(false)
            return await res.json()
        } else {
            Toast.show(await res.text())
        }
    },
    search: async function (query) {
        return await fetch(`/api/search?q=${query}`).then(res => res.json())
    },
    isAuthorized: async function () {
        return await fetch('/authorized').then(res => res.text()).then(text => text === 'true')
    }
}
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
const Search = {
    inputEl: document.querySelector('#tag-search-input'),
    resultsEl: document.querySelector('#search > ul'),
    resetBtn: document.querySelector('#search > div > span'),
    init: function () {
        this.resetBtn.onclick = () => {
            this.inputEl.value = ''
            this.resultsEl.innerHTML = ''
            this.inputEl.focus()
        }
        this.inputEl.onkeyup = () => {
            let value = this.inputEl.value.trim()
            if (value.length >= 2) {
                let frag = document.createDocumentFragment()
                Backend.search(encodeURIComponent(value)).then(results => {
                    if (results?.length > 0) {
                        results.forEach(result => {
                            frag.append(createSnippetLi(result))
                        })
                        this.resultsEl.innerHTML = ''
                        this.inputEl.style.color = 'var(--green)'
                        this.resultsEl.append(frag)
                    } else {
                        this.resultsEl.innerHTML = ''
                        this.inputEl.style.color = 'var(--red)'
                        this.resultsEl.appendChild(document.createElement('li')).textContent = 'Nothing found'
                    }
                })
            } else {
                this.resultsEl.innerHTML = ''
                this.inputEl.style.color = 'inherit'
            }
        }
    }
}

checker()

Search.init()
Editor.init()
Editor.new()
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
    if (!await Backend.isAuthorized()) window.location.href = '/login'
}

function updateSidebar(showToast = true) {
    let frag = document.createDocumentFragment()
    let openRadio = document.querySelector('#tags input:checked')?.id
    Backend.tags().then(async tags => {
        tags.forEach(tag => {
            let radio = document.createElement('input')
            radio.type = 'radio'
            radio.id = `tag-${tag.tag}`
            radio.name = 'tag'
            let div = document.createElement('div')
            div.className = 'tag'
            div.dataset.tag = tag.tag
            let label = document.createElement('label')
            label.htmlFor = `tag-${tag.tag}`
            label.onclick = (e) => {
                if (radio.checked) {
                    document.getElementById('no-tag').checked = true
                    document.querySelector(`label[for="tag-${tag.tag}"] span span`).textContent = '祈'
                    e.preventDefault()
                } else {
                    try {
                        unrollTag(document.querySelector(`#tag-${tag.tag}`).nextSibling)
                    } catch (e) {
                    }
                }
            }
            let span = document.createElement('span')
            span.textContent = `${tag.tag} (${tag.count})`
            span.appendChild(document.createElement('span')).textContent = '祈'
            label.appendChild(span)
            div.appendChild(label)
            div.appendChild(document.createElement('ul')).style = `min-height: calc(${tag.count} * (1em + 8px))`
            frag.appendChild(radio)
            frag.appendChild(div)
        })
        if (openRadio != null) {
            try {
                try {
                    await unrollTag(frag.querySelector(`#${openRadio} + .tag`))
                } catch (e) {
                }
            } catch (e) {
            }
        }
        document.querySelectorAll('.tag, #tags > div input[type=radio]').forEach(tag => {
            tag.remove()
        })
        document.querySelector('#tags > div').append(frag)
        if (showToast) Toast.show('Tags updated')
    })
}

async function unrollTag(el) {
    let snippets = await Backend.byTag(el.dataset.tag)
    el.previousSibling.checked = true
    el.querySelector('span span').textContent = '祉'
    snippets.forEach(snippet => {
        el.querySelector('ul').innerHTML = ''
        snippets.forEach(snippet => {
            el.querySelector('ul').append(createSnippetLi(snippet))
        })
    })
}

function createSnippetLi(snippetOverview) {
    let li = document.createElement('li')
    li.dataset.id = snippetOverview.id
    li.onclick = async () => {
        Editor.openSnippet(await Backend.snippet(snippetOverview.id))
    }
    li.appendChild(document.createElement('span')).textContent = snippetOverview.id
    li.append(snippetOverview.title)
    return li
}