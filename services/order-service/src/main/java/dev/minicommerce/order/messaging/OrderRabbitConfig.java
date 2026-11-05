package dev.minicommerce.order.messaging;

import dev.minicommerce.events.Messaging;
import dev.minicommerce.messaging.QueueDeclarations;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderRabbitConfig {

    @Bean
    TopicExchange eventsExchange() {
        return new TopicExchange(Messaging.EXCHANGE, true, false);
    }

    @Bean
    Declarables orderQueues(TopicExchange eventsExchange) {
        return QueueDeclarations.concat(
                QueueDeclarations.bindWithDlq(eventsExchange, Messaging.ORDER_INVENTORY_RESERVED_QUEUE, Messaging.INVENTORY_RESERVED),
                QueueDeclarations.bindWithDlq(eventsExchange, Messaging.ORDER_INVENTORY_REJECTED_QUEUE, Messaging.INVENTORY_REJECTED),
                QueueDeclarations.bindWithDlq(eventsExchange, Messaging.ORDER_PAYMENT_APPROVED_QUEUE, Messaging.PAYMENT_APPROVED),
                QueueDeclarations.bindWithDlq(eventsExchange, Messaging.ORDER_PAYMENT_FAILED_QUEUE, Messaging.PAYMENT_FAILED)
        );
    }
}
