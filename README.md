# Payment Service Provider

A Stripe-shaped payment service provider. Merchants create checkout
sessions, buyers pay on a hosted page, and card authorizations run
asynchronously through a transactional outbox and Kafka. The result is
delivered back to the merchant as a signed webhook. PostgreSQL is the
source of truth.

The API mirrors Stripe's shapes — `cs_` and `pm_` identifiers,
`Idempotency-Key` on session creation, `PROCESSING` while an authorization
is in flight — so a merchant integration written against Stripe maps onto
this service.

## Stack

- Java 17, Spring Boot 4
- PostgreSQL 16
- Kafka 7.6 (KRaft, single node)
- Spring Kafka

## Requirements

* java 17+
* docker 27+ (for PostgreSQL and Kafka)

## Setup instructions

```
git clone <this-repo>
cd payment-service-provider
docker compose up -d
./mvnw spring-boot:run
```

The app starts on **http://localhost:8085**.

Compose exposes PostgreSQL on `5432` and Kafka on `9093`.

Webhook delivery is opt-in:

```
psp.webhook.enabled=true
psp.webhook.url=http://localhost:8081/webhooks/psp
psp.webhook.secret=whsec_local_dev_secret
```

## API summary

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/v1/checkout/sessions` | `Idempotency-Key` | Create a checkout session for an amount and currency |
| GET | `/v1/checkout/sessions/{id}` | Public | Fetch session status, amount, and last error |
| POST | `/v1/checkout/sessions/{id}/confirm` | Public | Claim the session and enqueue authorization (`202`) |
| POST | `/v1/payment_methods` | Public | Tokenize card details, returns a `pm_` id |
| GET | `/checkout/{id}` | Public | Hosted checkout page for the session |

Typical flow:

```
→ POST /v1/payment_methods                          (tokenize card → pm_…)
→ POST /v1/checkout/sessions                        (Idempotency-Key)
→ GET /checkout/{cs_id}                             (buyer enters card)
→ POST /v1/checkout/sessions/{cs_id}/confirm        (paymentMethodId) → 202
→ GET /v1/checkout/sessions/{cs_id}                 (PROCESSING → COMPLETE | OPEN + error)
→ webhook: checkout.session.completed | checkout.session.payment_failed
```

Outcomes are delivered to the merchant as `Psp-Signature` signed events.

## Design decisions

* Payment methods are tokenized before confirm: `POST /v1/payment_methods`
  validates the number (Luhn, length, expiry) and stores it, returning a
  `pm_` identifier. Confirm accepts only that identifier, so a checkout
  request never carries raw card fields and the flow survives a swap to a
  real provider.

* Authorization is asynchronous through a transactional outbox: Confirm
  moves the session to `PROCESSING`, appends an authorization row, and
  commits; a poller publishes it to Kafka; a listener authorizes and writes
  the result. There is no dual write between PostgreSQL and the broker, and
  no request thread is held waiting on authorization.

* The outbox claims by lease: Rows are selected with
  `FOR UPDATE SKIP LOCKED` and stamped with a claim time. A publisher that
  crashes between claim and publish does not lose the event — the row
  becomes claimable again once the lease expires, because the alternative,
  a lost event, is worse than a duplicate.

* Consumer idempotency lives in the state machine: The listener locks the
  session row and re-checks `PROCESSING`. A redelivered record finds the
  session already settled and becomes a no-op, so at-least-once delivery
  does not authorize twice.

* Confirm is the double-charge guard: The session leaves `OPEN` in the same
  transaction that enqueues the authorization. A second confirm sees a
  non-open session and returns the stored result without enqueuing anything
  again.

* A decline is retryable: A declined authorization returns the session to
  `OPEN` with `last_error_code` and `last_error_message`. The buyer can retry
  on the same session. `COMPLETE` is terminal; `OPEN` after a decline is not.

* Metadata is relational: Session metadata is stored one row per key in its
  own table rather than as a serialized blob, so it stays queryable and
  indexable.

* Webhooks have their own outbox: Delivery rows are written in the same
  transaction as the session update, with the payload and target URL frozen
  at write time. A scheduled publisher retries with exponential backoff
  until delivery succeeds or the attempt budget is exhausted.

* Webhooks are signed for the receiver: `Psp-Signature` carries
  `t=<unix>,v1=<hmac-sha256 of "t.payload">`, and every event has a unique
  id. The merchant gets replay protection, integrity, and a dedupe key in
  one header.

* The card never touches merchant origin: The hosted page under
  `/checkout/{id}` talks to the tokenization endpoint directly. The merchant
  receives identifiers and events, never a card number.

* Soft delete preserves history: Rows are flagged `deleted` rather than
  removed, keeping sessions, payment methods, and delivery attempts
  auditable.
