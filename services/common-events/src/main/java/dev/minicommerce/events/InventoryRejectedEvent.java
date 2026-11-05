package dev.minicommerce.events;

import java.time.Instant;
import java.util.UUID;

public record InventoryRejectedEvent(
        UUID eventId,
        UUID orderId,
        String reason,
        Instant occurredAt
) {
}
