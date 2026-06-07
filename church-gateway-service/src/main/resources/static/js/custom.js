/* =============================================
   Imanuel Jakarta Church - Custom JavaScript
   ============================================= */

document.addEventListener('DOMContentLoaded', function () {

    // Auto-dismiss flash messages after 5 seconds
    const alerts = document.querySelectorAll('.alert.auto-dismiss');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });

    // Delete confirmation dialogs
    const deleteForms = document.querySelectorAll('form[data-confirm]');
    deleteForms.forEach(function (form) {
        form.addEventListener('submit', function (e) {
            const message = form.getAttribute('data-confirm') || 'Are you sure you want to delete this item?';
            if (!confirm(message)) {
                e.preventDefault();
            }
        });
    });

    // Admin sidebar active link
    const currentPath = window.location.pathname;
    const sidebarLinks = document.querySelectorAll('.admin-sidebar .nav-link');
    sidebarLinks.forEach(function (link) {
        if (link.getAttribute('href') && currentPath.startsWith(link.getAttribute('href')) && link.getAttribute('href') !== '/admin') {
            link.classList.add('active');
        } else if (link.getAttribute('href') === '/admin' && currentPath === '/admin') {
            link.classList.add('active');
        }
    });

    // Mobile admin sidebar toggle
    const adminToggle = document.getElementById('adminSidebarToggle');
    const adminSidebar = document.querySelector('.admin-sidebar');
    if (adminToggle && adminSidebar) {
        adminToggle.addEventListener('click', function () {
            adminSidebar.classList.toggle('show');
        });
    }

    // YouTube iframe height adjustment
    const iframes = document.querySelectorAll('.livestream-iframe');
    iframes.forEach(function (iframe) {
        const container = iframe.closest('.livestream-container');
        if (container) {
            const width = container.offsetWidth;
            iframe.style.height = Math.round(width * 9 / 16) + 'px';
        }
    });

    // Event countdown timer
    const countdownEls = document.querySelectorAll('[data-countdown]');
    countdownEls.forEach(function (el) {
        const targetDate = new Date(el.getAttribute('data-countdown'));
        if (isNaN(targetDate.getTime())) return;
        updateCountdown(el, targetDate);
        setInterval(function () { updateCountdown(el, targetDate); }, 1000);
    });

    function updateCountdown(el, targetDate) {
        const now = new Date();
        const diff = targetDate - now;
        if (diff <= 0) {
            el.textContent = 'Event has started!';
            return;
        }
        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((diff % (1000 * 60)) / 1000);
        el.textContent = days + 'd ' + hours + 'h ' + minutes + 'm ' + seconds + 's';
    }

    // Newsletter form inline submission
    const newsletterForm = document.getElementById('newsletterForm');
    if (newsletterForm) {
        newsletterForm.addEventListener('submit', function (e) {
            const emailInput = newsletterForm.querySelector('input[type="email"]');
            if (emailInput && !emailInput.value.includes('@')) {
                e.preventDefault();
                emailInput.classList.add('is-invalid');
            }
        });
    }
});
