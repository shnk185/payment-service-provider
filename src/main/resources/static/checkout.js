const sessionId = window.location.pathname.split('/').filter(Boolean).pop();
const POLL_INTERVAL_MS = 1000;
const POLL_MAX_ATTEMPTS = 60;

const el = (id) => document.getElementById(id);

const amountEl = el('amount');
const statusBadge = el('status-badge');
const messageEl = el('message');
const payButton = el('pay-button');
const cancelButton = el('cancel-button');
const form = el('payment-form');

let session = null;

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

function formatAmount(amount, currency) {
    if (amount == null) return '—';
    try {
        return new Intl.NumberFormat(undefined, { style: 'currency', currency }).format(amount);
    } catch (err) {
        return `${amount} ${currency ?? ''}`.trim();
    }
}

async function errorText(res, fallback) {
    try {
        const body = await res.json();
        return body.message || body.detail || fallback;
    } catch (err) {
        return fallback;
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

function render(next) {
    session = next;
    amountEl.textContent = formatAmount(next.amount, next.currency);
    statusBadge.textContent = next.status;
    statusBadge.dataset.status = next.status;
}

function isPastExpiry(current) {
    return current.expiresAt != null && new Date(current.expiresAt) <= new Date();
}

function readCard() {
    const parts = el('card-expiry').value.split(/[^0-9]+/).filter(Boolean);
    return {
        number: el('card-number').value.replace(/[\s-]/g, ''),
        expMonth: Number(parts[0]),
        expYear: parts.length > 1 ? 2000 + Number(parts[1]) : null,
        cvc: el('card-cvc').value,
    };
}

async function tokenize(card) {
    const res = await fetch('/v1/payment_methods', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type: 'CARD', card }),
    });
    if (!res.ok) throw new Error(await errorText(res, `Card check failed (${res.status})`));
    return (await res.json()).id;
}

async function pollUntilSettled(current) {
    let state = current;
    for (let attempt = 0; attempt < POLL_MAX_ATTEMPTS; attempt++) {
        if (state.status !== 'PROCESSING') return state;
        await sleep(POLL_INTERVAL_MS);
        const res = await fetch(`/v1/checkout/sessions/${sessionId}`);
        if (!res.ok) throw new Error(await errorText(res, `Could not load session (${res.status})`));
        state = await res.json();
        render(state);
    }
    if (state.status === 'PROCESSING') {
        throw new Error('Payment is still processing. Try again in a moment.');
    }
    return state;
}

async function pay() {
    setButtonsEnabled(false);
    showMessage('Processing…', 'info');
    try {
        const paymentMethodId = await tokenize(readCard());

        const res = await fetch(`/v1/checkout/sessions/${sessionId}/confirm`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ paymentMethodId }),
        });
        if (!res.ok) throw new Error(await errorText(res, `Confirm failed (${res.status})`));

        const settled = await pollUntilSettled(await res.json());
        render(settled);

        if (settled.status === 'COMPLETE') {
            showMessage('Payment complete.', 'success');
            if (settled.successUrl) window.location.href = settled.successUrl;
            return;
        }
        showMessage(settled.error?.message ?? 'Payment was not completed.', 'error');
        setButtonsEnabled(true);
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
        if (!res.ok) throw new Error(await errorText(res, `Could not load session (${res.status})`));

        render(await res.json());

        if (isPastExpiry(session)) {
            showMessage('This checkout session has expired.', 'error');
            return;
        }
        if (session.status === 'PROCESSING') {
            showMessage('A payment is already in progress…', 'info');
            const settled = await pollUntilSettled(session);
            render(settled);
            if (settled.status === 'COMPLETE' && settled.successUrl) {
                window.location.href = settled.successUrl;
                return;
            }
            showMessage(settled.error?.message ?? 'This checkout session is no longer open.', 'error');
            return;
        }
        if (session.status !== 'OPEN') {
            showMessage('This checkout session is no longer open.', 'error');
            return;
        }
        setButtonsEnabled(true);
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

form.addEventListener('submit', (event) => {
    event.preventDefault();
    pay();
});

cancelButton.addEventListener('click', () => {
    if (session?.cancelUrl) {
        window.location.href = session.cancelUrl;
    } else {
        showMessage('No return URL configured.', 'error');
    }
});

init();