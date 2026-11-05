package dev.minicommerce.payment.api;

import dev.minicommerce.payment.domain.Payment;
import dev.minicommerce.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID orderId,
        String sku,
        BigDecimal amount,
        PaymentStatus status,
        String reason,
        Instant createdAt
) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getOrderId(),
                payment.getSku(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getReason(),
                payment.getCreatedAt()
        );
    }
}
