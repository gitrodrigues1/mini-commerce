package dev.minicommerce.payment.messaging;

import dev.minicommerce.events.InventoryReservedEvent;
import dev.minicommerce.events.Messaging;
import dev.minicommerce.payment.application.PaymentApplicationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListeners {

    private final PaymentApplicationService payments;

    public PaymentEventListeners(PaymentApplicationService payments) {
        this.payments = payments;
    }

    @RabbitListener(queues = Messaging.PAYMENT_INVENTORY_RESERVED_QUEUE, concurrency = "1")
    public void onReserved(InventoryReservedEvent event) {
        payments.charge(event);
    }
}
