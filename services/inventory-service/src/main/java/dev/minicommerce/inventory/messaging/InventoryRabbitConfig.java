package dev.minicommerce.inventory.messaging;

import dev.minicommerce.events.Messaging;
import dev.minicommerce.messaging.QueueDeclarations;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryRabbitConfig {

    @Bean
    TopicExchange eventsExchange() {
        return new TopicExchange(Messaging.EXCHANGE, true, false);
    }

    @Bean
    Declarables inventoryQueues(TopicExchange eventsExchange) {
        return QueueDeclarations.concat(
                QueueDeclarations.bindWithDlq(eventsExchange, Messaging.INVENTORY_ORDER_CREATED_QUEUE, Messaging.ORDER_CREATED),
                QueueDeclarations.bindWithDlq(eventsExchange, Messaging.INVENTORY_PAYMENT_FAILED_QUEUE, Messaging.PAYMENT_FAILED),
                QueueDeclarations.bindWithDlq(eventsExchange, Messaging.INVENTORY_PAYMENT_APPROVED_QUEUE, Messaging.PAYMENT_APPROVED)
        );
    }
}
