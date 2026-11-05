package dev.minicommerce.order.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderStatusTest {

    @Test
    void pendingCanBeReservedOrRejected() {
        assertEquals(OrderStatus.RESERVED, OrderStatus.PENDING.advanceTo(OrderStatus.RESERVED));
        assertEquals(OrderStatus.REJECTED, OrderStatus.PENDING.advanceTo(OrderStatus.REJECTED));
        assertEquals(OrderStatus.PENDING, OrderStatus.PENDING.advanceTo(OrderStatus.PAID));
    }

    @Test
    void reservedCanBePaidOrFailed() {
        assertEquals(OrderStatus.PAID, OrderStatus.RESERVED.advanceTo(OrderStatus.PAID));
        assertEquals(OrderStatus.PAYMENT_FAILED, OrderStatus.RESERVED.advanceTo(OrderStatus.PAYMENT_FAILED));
    }

    @Test
    void terminalStatusDoesNotMove() {
        assertEquals(OrderStatus.PAID, OrderStatus.PAID.advanceTo(OrderStatus.PAYMENT_FAILED));
        assertEquals(OrderStatus.REJECTED, OrderStatus.REJECTED.advanceTo(OrderStatus.RESERVED));
    }
}
