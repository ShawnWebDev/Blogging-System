// needed for full page load, HTMX will not fire it using hx-on
document.addEventListener("DOMContentLoaded", initSidebar);

// todo: add Intersection Observer to highlight the sidebar nav item that the page heading is related to.

function initSidebar() {
    console.log("Sidebar Check..");
    const textContainer = document.getElementById("post-text");
    if (!textContainer) return;

    const sidebarNavContainer = document.getElementById("sidebar-menu");
    if (sidebarNavContainer.innerHTML.startsWith('<strong>')) return;
    console.log("Initializing Sidebar..");

    const headers = textContainer.querySelectorAll("h2, h3");

    let list = [`<strong>Table Of Contents</strong><ol>`];
    let isH2Active = false;
    let hasNested = false;

    headers.forEach(el => {
        // remove anything not a Word
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
    });

    if (hasNested) list.push('</ul>');
    list.push('</ol>');
    sidebarNavContainer.innerHTML = list.join('');
}

function createListItem(id, text) {
    return `
    <li>
        <a href="#${id}" class="sidebar-nav-item">${text}</a>
    </li>
`;
}

