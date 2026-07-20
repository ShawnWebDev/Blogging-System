// needed for full page load, HTMX will not fire it using hx-on
document.addEventListener("DOMContentLoaded", initSidebar);

let headingOrder;
let headingIndexMap;

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
            } else if (entry.boundingClientRect.top > 10) {
                if (navItem.classList.contains("active-nav-link")) {
                    navItem.classList.remove("active-nav-link");
                    const idx = headingIndexMap.get(entry.target.id);
                    const prevId = idx > 0 ? headingOrder[idx - 1] : null;
                    const prevItem = prevId && document.querySelector(`#sidebar-item-${prevId}`);
                    if (prevItem) {
                        prevItem.classList.add("active-nav-link");
                    }
                }
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
    headingOrder = [];
    headingIndexMap = new Map();

    headers.forEach(el => {
        // remove symbols and whitespace
        const elementId = el.textContent.replace(/\W/g, '');
        el.id = elementId;
        headingIndexMap.set(elementId, headingOrder.length);
        headingOrder.push(elementId);

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
    if (isSinglePostPage(window.location.pathname)) {
        list.push('<a href="#comments-section" class="sidebar-nav-item btn-secondary" id="toCommentsAnchor">To Comment Section</a>');
    }
    sidebar.innerHTML = list.join('');
}

function toggleSidebar() {
    document.getElementById("sidebar-menu").classList.toggle("toc-open");
    document.getElementById("sidebar-toggle").classList.toggle("toc-open-btn");
}

function isSinglePostPage(path) {
    const pathname = path.split("/");
    if (pathname.length < 3) return false;
    return pathname[1] === "blog" && pathname[2] === "post";
}