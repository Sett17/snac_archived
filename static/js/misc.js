import * as Backend from '/assets/js/backend.js'
import * as Editor from '/assets/js/editor.js'
import * as Toast from '/assets/js/toast.js'

export async function checker() {
    if (!await Backend.isAuthorized()) window.location.href = '/login'
}

export function updateSidebar(showToast = true) {
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

export async function unrollTag(el) {
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

export function createSnippetLi(snippetOverview) {
    let li = document.createElement('li')
    li.dataset.id = snippetOverview.id
    li.onclick = async () => {
        Editor.openSnippet(await Backend.snippet(snippetOverview.id))
    }
    li.appendChild(document.createElement('span')).textContent = snippetOverview.id
    li.append(snippetOverview.title)
    return li
}