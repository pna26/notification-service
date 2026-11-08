package store.notification;

import java.time.Instant;
import java.util.UUID;

/**
 * A visão do consumidor sobre o evento do account. Campos desconhecidos são
 * ignorados (tolerant reader): o account pode acrescentar campos sem quebrar este serviço.
 */
public record AccountEvent(
        UUID eventId,
        String eventType,
        String accountId,
        String name,
        String email,
        Instant occurredAt) {
}
