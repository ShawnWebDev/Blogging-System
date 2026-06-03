// needed for full page load, HTMX will not fire it using hx-on
document.addEventListener("DOMContentLoaded", initSidebar);

const observer = new IntersectionObserver(entries => {
    if (!document.getElementById("post-text")) {
        observer.disconnect();
        return;
    }

    entries.forEach(entry => {
        document.querySelector(`#sidebar-item-${entry.target.id}`).classList.toggle("active-nav-link", entry.isIntersecting);
    })
},{
    threshold: 0.95,
    rootMargin: "-10% 0px -45% 0px"
})

function createListItem(id, text) {
    return `
    <li>
        <a href="#${id}" class="sidebar-nav-item ${id}" id="sidebar-item-${id}">${text}</a>
    </li>
`;
}

function initSidebar() {
    const textContainer = document.getElementById("post-text");
    if (!textContainer) return;

    const sidebar = document.getElementById('sidebar-menu');
    if (sidebar.innerHTML.length !== 0) return;

    const headers = textContainer.querySelectorAll("h2, h3");
    let list = [`<strong>Table Of Contents</strong><ol>`];
    let isH2Active = false;
    let hasNested = false;

    headers.forEach(el => {
        // remove symbols and whitespace
        const elementId = el.textContent.replace(/\W/g, '');
        el.id = elementId;

        if (el.tagName === 'H2') {
            if (hasNested) {
                list.push('</ul>');
                hasNested = false;
            }
            isH2Active = true;
            list.push(createListItem(elementId, el.textContent));
        }
        else if (el.tagName === 'H3' && isH2Active) {
            if (!hasNested) {
                list.push('<ul>');
                hasNested = true;
            }
            list.push(createListItem(elementId, el.textContent));
        }
        observer.observe(el);
    });

    if (hasNested) list.push('</ul>');
    list.push('</ol>');
    sidebar.innerHTML = list.join('');
}
