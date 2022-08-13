import * as Toast from '/assets/js/toast.js'
import * as Misc from '/assets/js/misc.js'

export async function tags() {
    return await fetch('/api/tags').then(res => res.json())
}

export async function byTag(tag) {
    return await fetch(`/api/tag/${tag}`).then(res => res.json())
}

export async function snippet(id) {
    return await fetch(`/api/snippet/${id}`).then(res => res.json())
}

export async function deleteSnippet(id) {
    return await fetch(`/api/snippet/${id}`, {
        method: 'DELETE'
    }).then(res => res.ok)
}

export async function updateSnippet(snippet) {
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
}

export async function newSnippet(snippet) {
    let res = await fetch('/api/new', {
        method: 'POST',
        body: JSON.stringify(snippet)
    })
    if (res.status === 200) {
        Toast.show('Snippet created')
        Misc.updateSidebar(false)
        return await res.json()
    } else {
        Toast.show(await res.text())
    }
}

export async function search(query) {
    return await fetch(`/api/search?q=${query}`).then(res => res.json())
}

export async function isAuthorized() {
    return await fetch('/authorized').then(res => res.text()).then(text => text === 'true')
}