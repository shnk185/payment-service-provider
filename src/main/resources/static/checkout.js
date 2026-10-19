const sessionId = window.location.pathname.split('/').filter(Boolean).pop();
const el = (id) => document.getElementById(id);

const amountEl = el('amount');
const statusBadge = el('status-badge');
const messageEl = el('message');
const payButton = el('pay-button');
const cancelButton = el('cancel-button');
const form = el('payment-form');

function formatAmount(amount, currency) {
    if (amount == null) return '—';
    try {
        return new Intl.NumberFormat(undefined, { style: 'currency', currency }).format(amount);
    } catch (err) {
        return `${amount} ${currency ?? ''}`.trim();
    }
}

function showMessage(text, kind = 'error') {
    messageEl.textContent = text;
    messageEl.className = `message message--${kind}`;
}

function setButtonsEnabled(enabled) {
    payButton.disabled = !enabled;
    cancelButton.disabled = !enabled;
}

function render(session) {
    amountEl.textContent = formatAmount(session.amount, session.currency);
    statusBadge.textContent = session.status;
    statusBadge.dataset.status = session.status;
}

function isExpired(session) {
    return session.status !== 'OPEN'
        || (session.expiresAt != null && new Date(session.expiresAt) <= new Date());
}

async function confirm(outcome) {
    setButtonsEnabled(false);
    showMessage('Processing…', 'info');
    try {
        const res = await fetch(`/v1/checkout/sessions/${sessionId}/confirm`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ outcome }),
        });
        if (!res.ok) throw new Error(`Confirm failed (${res.status})`);
        const body = await res.json();
        if (body.redirectUrl) {
            window.location.href = body.redirectUrl;
        } else {
            render(body);
            showMessage('Payment recorded.', 'success');
            setButtonsEnabled(false);
        }
    } catch (err) {
        showMessage(err.message, 'error');
        setButtonsEnabled(true);
    }
}

async function init() {
    if (!sessionId || !sessionId.startsWith('cs_')) {
        statusBadge.textContent = 'INVALID';
        showMessage('Missing checkout session id.');
        return;
    }
    try {
        const res = await fetch(`/v1/checkout/sessions/${sessionId}`);
        if (res.status === 404) {
            statusBadge.textContent = 'NOT_FOUND';
            statusBadge.dataset.status = 'NOT_FOUND';
            showMessage('Checkout session not found.');
            return;
        }
        if (!res.ok) throw new Error(`Could not load session (${res.status})`);

        const session = await res.json();
        render(session);

        if (isExpired(session)) {
            showMessage('This checkout session is no longer open.');
            return;
        }
        setButtonsEnabled(true);
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

form.addEventListener('submit', (event) => {
    event.preventDefault();
    confirm('SUCCESS');
});
cancelButton.addEventListener('click', () => confirm('FAILED'));

init();
