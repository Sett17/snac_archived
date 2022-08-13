document.querySelector("#tags span button:nth-child(2)").onclick = () => {
    updateSidebar()
}
updateSidebar(false)

async function updateSidebar(showToast = false) {
    let html = await (await fetch('/render/tags')).text();
    document.querySelector('#tags > div').innerHTML = html;
}