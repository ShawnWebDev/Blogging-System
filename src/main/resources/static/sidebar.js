// needed for full page load, HTMX will not fire it using hx-on
document.addEventListener("DOMContentLoaded", initSidebar);

const observer = new IntersectionObserver(entries => {
    if (!document.getElementById("post-text")) {
        observer.disconnect();
        return;
    }

    entries.forEach(entry => {
        const navItem = document.querySelector(`#sidebar-item-${entry.target.id}`);
        if (entry.isIntersecting) {
            navItem.classList.add("active-nav-link");
        } else if (entry.boundingClientRect.top > 0) {
            navItem.classList.remove("active-nav-link");
        }
    })

    document.querySelector(".last-active-nav-link")?.classList.remove("last-active-nav-link");
    const activeLinks = document.querySelectorAll('.sidebar-nav-item.active-nav-link');
    if (activeLinks.length > 0) {
        activeLinks[activeLinks.length - 1].classList.add("last-active-nav-link");
    }
},{
    threshold: 1,
    rootMargin: "0px 0px -50% 0px"
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
    let list = [`<strong>Table of Contents:</strong><ol>`];
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
