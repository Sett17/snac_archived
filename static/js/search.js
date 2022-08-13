let inputEl = document.querySelector('#tag-search-input')
let resultsEl = document.querySelector('#search > ul')
let resetBtn = document.querySelector('#search > div > span')

export function init() {
    resetBtn.onclick = () => {
        inputEl.value = ''
        resultsEl.innerHTML = ''
        inputEl.focus()
    }
    inputEl.onkeyup = () => {
        let value = inputEl.value.trim()
        if (value.length >= 2) {
            let frag = document.createDocumentFragment()
            Backend.search(encodeURIComponent(value)).then(results => {
                if (results?.length > 0) {
                    results.forEach(result => {
                        frag.append(createSnippetLi(result))
                    })
                    resultsEl.innerHTML = ''
                    inputEl.style.color = 'var(--green)'
                    resultsEl.append(frag)
                } else {
                    resultsEl.innerHTML = ''
                    inputEl.style.color = 'var(--red)'
                    resultsEl.appendChild(document.createElement('li')).textContent = 'Nothing found'
                }
            })
        } else {
            resultsEl.innerHTML = ''
            inputEl.style.color = 'inherit'
        }
    }
}