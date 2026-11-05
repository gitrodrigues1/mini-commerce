package dev.minicommerce.order.domain;

public enum OrderStatus {
    PENDING,
    RESERVED,
    REJECTED,
    PAID,
    PAYMENT_FAILED;

    public OrderStatus advanceTo(OrderStatus next) {
        return switch (this) {
            case PENDING -> switch (next) {
                case RESERVED, REJECTED -> next;
                default -> this;
            };
            case RESERVED -> switch (next) {
                case PAID, PAYMENT_FAILED -> next;
                default -> this;
            };
            default -> this;
        };
    }
}
