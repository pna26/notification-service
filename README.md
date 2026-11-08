# notification-service

Consumidor de eventos da loja (bottleneck de **mensageria**, seguindo o Hands-on 7 do curso).

- Lê o tópico `account-events` do Kafka (grupo `notification`), publicado pelo `account-service`
  por meio de um *transactional outbox*.
- Para cada `AccountCreated`, envia um e-mail de boas-vindas (Mailpit no compose/k8s).
- **Idempotente:** grava o `eventId` em `notification.processed_events` na mesma transação do registro do
  e-mail; um evento repetido (entrega *at-least-once*) é ignorado.
- **Retries e DLT:** 3 novas tentativas com *backoff* exponencial (0,5 s, 1 s, 2 s); JSON inválido ou evento
  sem os campos obrigatórios vão direto para `account-events-dlt` e ficam em `notification.dead_letters`.

Documentação: <https://pna26.github.io/platform/bottlenecks/messaging/>

## Variáveis de ambiente

| Variável | Padrão | Uso |
|---|---|---|
| `DATABASE_HOST`, `DATABASE_PORT`, `DATABASE_DB` | — | PostgreSQL (schema `notification`) |
| `DATABASE_USERNAME`, `DATABASE_PASSWORD` | — | credenciais do banco (Secret no k8s, nunca no Git) |
| `KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` | broker Kafka |
| `MAIL_HOST`, `MAIL_PORT` | `mailpit`, `1025` | servidor SMTP |
| `NOTIFY_CONCURRENCY` | `3` | threads consumidoras (no máximo 6 úteis: 6 partições) |
| `EMAIL_LATENCY` | `50ms` | latência simulada do provedor de e-mail |
| `NOTIFY_IDEMPOTENT` | `true` | `false` desliga a deduplicação (só para o experimento) |

## Build

```bash
./mvnw clean package
docker build -t notification-service .
```
