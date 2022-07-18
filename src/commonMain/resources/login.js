let form = document.forms[0]

form.onsubmit = async function (e) {
    e.preventDefault()
    let resp = await fetch('/login', {
        method: 'POST',
        body: new FormData(form)
    })
    if (resp.ok) {
        let data = await resp.json()
        document.cookie = `token=${data.token}; expires=${data.expires}`
        window.location.href = '/'
    } else {
        window.location.href = '/login'
    }
}