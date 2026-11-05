package dev.minicommerce.inventory.messaging;

import dev.minicommerce.events.Messaging;
import dev.minicommerce.events.OrderCreatedEvent;
import dev.minicommerce.events.PaymentApprovedEvent;
import dev.minicommerce.events.PaymentFailedEvent;
import dev.minicommerce.inventory.application.InventoryApplicationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListeners {

    private final InventoryApplicationService inventory;

    public InventoryEventListeners(InventoryApplicationService inventory) {
        this.inventory = inventory;
    }

    @RabbitListener(queues = Messaging.INVENTORY_ORDER_CREATED_QUEUE, concurrency = "1")
    public void onOrderCreated(OrderCreatedEvent event) {
        inventory.reserve(event);
    }

    @RabbitListener(queues = Messaging.INVENTORY_PAYMENT_FAILED_QUEUE, concurrency = "1")
    public void onPaymentFailed(PaymentFailedEvent event) {
        inventory.release(event);
    }

    @RabbitListener(queues = Messaging.INVENTORY_PAYMENT_APPROVED_QUEUE, concurrency = "1")
    public void onPaymentApproved(PaymentApprovedEvent event) {
        inventory.confirm(event);
    }
}
