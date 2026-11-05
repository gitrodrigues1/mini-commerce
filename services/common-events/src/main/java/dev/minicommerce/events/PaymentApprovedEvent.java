package dev.minicommerce.events;

import java.time.Instant;
import java.util.UUID;

public record PaymentApprovedEvent(
        UUID eventId,
        UUID orderId,
        Instant occurredAt
) {
}
