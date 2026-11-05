package dev.minicommerce.order.messaging;

import dev.minicommerce.events.InventoryRejectedEvent;
import dev.minicommerce.events.InventoryReservedEvent;
import dev.minicommerce.events.Messaging;
import dev.minicommerce.events.PaymentApprovedEvent;
import dev.minicommerce.events.PaymentFailedEvent;
import dev.minicommerce.order.application.OrderApplicationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListeners {

    private final OrderApplicationService orders;

    public OrderEventListeners(OrderApplicationService orders) {
        this.orders = orders;
    }

    @RabbitListener(queues = Messaging.ORDER_INVENTORY_RESERVED_QUEUE, concurrency = "1")
    public void onReserved(InventoryReservedEvent event) {
        orders.onReserved(event);
    }

    @RabbitListener(queues = Messaging.ORDER_INVENTORY_REJECTED_QUEUE, concurrency = "1")
    public void onRejected(InventoryRejectedEvent event) {
        orders.onRejected(event);
    }

    @RabbitListener(queues = Messaging.ORDER_PAYMENT_APPROVED_QUEUE, concurrency = "1")
    public void onPaid(PaymentApprovedEvent event) {
        orders.onPaid(event);
    }

    @RabbitListener(queues = Messaging.ORDER_PAYMENT_FAILED_QUEUE, concurrency = "1")
    public void onPaymentFailed(PaymentFailedEvent event) {
        orders.onPaymentFailed(event);
    }
}
