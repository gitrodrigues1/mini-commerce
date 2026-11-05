package dev.minicommerce.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        UUID orderId,
        String sku,
        int quantity,
        BigDecimal amount,
        Instant occurredAt
) {
}
