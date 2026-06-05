
const navItems = document.querySelectorAll('.nav-item');
navItems.forEach(el => {
    el.addEventListener('click', function () {
        navItems.forEach(el_2 => el_2.classList.remove('active'));
        this.classList.add('active');
    })
})

document.addEventListener("htmx:configRequest", function(e) {
    const el = document.getElementById("csrf-token");
    if (el) {
    e.detail.headers[el.dataset.header] = el.dataset.token;
    }
})

document.body.addEventListener('loginSuccess', function(e) {
    document.querySelectorAll('.no-auth-msg').forEach(el => el.remove());
    document.querySelectorAll('.comment').forEach((el) => {
        let commentId = el.dataset.commentid;
        let entryId = el.dataset.entryid;
        let commentAuthor = el.dataset.authorname;
        let knownName = e.detail.value;
        let deleted = el.dataset.deleted === "true" || el.dataset.deleted === true;
        if (knownName === commentAuthor) {
            // Service confirms the author & id are related
            let div = el.querySelector('.comment-author-btns');
            div.innerHTML = `
                      <button class="btn-secondary edit-btn" hx-get="/comment/commentComponent/editForm?entryId=${entryId}&commentId=${commentId}"
                              hx-target="#reply-form-container-${commentId}" hx-swap="beforeend">Edit</button>
                      ${deleted ? '' : `<button class="btn-secondary" hx-delete="/comment/delete?entryId=${entryId}&commentId=${commentId}" hx-swap="delete">Delete</button>`}
                    `;
            htmx.process(div);
        }
    })
})

document.body.addEventListener('navigationChange', () => {
    document.getElementById("portfolio-nav-item").classList.remove("active");
    document.getElementById("blog-nav-item").classList.add("active");
})

let refreshTimer;

//close dialog after 30 mins
function startCloseDialogTimer(elementId) {
    refreshTimer = setTimeout(() => {
        closeDialog(elementId);
    }, 60000 * 30)
}

function closeDialog(elementId) {
    document.getElementById(elementId).close();
    clearTimeout(refreshTimer);
}

function openDialog(elementId) {
    fetchNewCsrfToken();
    document.getElementById(elementId).showModal();
    startCloseDialogTimer(elementId);
}

function removeElement(elementId) {
    document.getElementById(elementId).remove();
}

function fetchNewCsrfToken() {
    htmx.ajax('GET', '/refresh-token', {swap: 'none'});
}

// TODO -->
function convertTimeToLocal() {
    console.log('login message fragment');
    const timeEl = document.getElementById('timeExpiration');
    if (!timeEl) {
        return;
    }
    console.log("time element exists.. convert: " + timeEl.innerText);
}