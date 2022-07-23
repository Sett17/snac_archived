let form = document.forms[0]

form.onsubmit = async function (e) {
    e.preventDefault()
    let resp = await fetch('/login', {
        method: 'POST',
        body: new FormData(form)
    })
    if (resp.ok) {
        window.location.href = '/'
    } else {
        window.location.href = '/login'
    }
}