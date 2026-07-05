// needed for full page load, HTMX will not fire it using hx-on
document.addEventListener("DOMContentLoaded", initSidebar);

const visObserver = new IntersectionObserver(entries => {
    if (!document.getElementById("post-text")) {
        visObserver.disconnect();
        return;
    }
    const allNavItems = document.querySelectorAll('.sidebar-nav-item');
    entries.forEach(entry => {
        const navItem = document.querySelector(`#sidebar-item-${entry.target.id}`);
        if (navItem) {
            if (entry.isIntersecting) {
                allNavItems.forEach(item => {
                    item.classList.remove("active-nav-link");
                })
                navItem.classList.add("active-nav-link");
                navItem.classList.add("viewed-nav-link");
            } else if (entry.boundingClientRect.top > 10) {
                navItem.classList.remove("viewed-nav-link");
            }
        }
    })

},{
    threshold: 0,
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
    if (!sidebar || sidebar.children.length !== 0) return;

    const headers = textContainer.querySelectorAll("h2, h3");
    let list = [`<div><strong>Table of Contents</strong><button id="close-sidebar-btn" class="btn-primary" onclick="toggleSidebar()">X</button></div><ol>`];
    let hasNested = false;

    headers.forEach(el => {
        // remove symbols and whitespace
        const elementId = el.textContent.replace(/\W/g, '');
        el.id = elementId;

        if (el.tagName === 'H2') {
            if (hasNested) {
                list.push('</ul></li>');
                hasNested = false;
            }
            list.push(createListItem(elementId, el.textContent));
        }
        else if (el.tagName === 'H3') {
            if (!hasNested) {
                list.push('<li><ul>');
                hasNested = true;
            }
            list.push(createListItem(elementId, el.textContent));
        }
        visObserver.observe(el);
    });

    if (hasNested) list.push('</ul>');
    list.push('</ol>');
    sidebar.innerHTML = list.join('');
}

function toggleSidebar() {
    console.log("toggleSidebar ");
    document.getElementById("sidebar-menu").classList.toggle("toc-open");
    document.getElementById("sidebar-toggle").classList.toggle("toc-open-btn");
}
