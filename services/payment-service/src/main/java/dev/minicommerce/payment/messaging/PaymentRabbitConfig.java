package dev.minicommerce.payment.messaging;

import dev.minicommerce.events.Messaging;
import dev.minicommerce.messaging.QueueDeclarations;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentRabbitConfig {

    @Bean
    TopicExchange eventsExchange() {
        return new TopicExchange(Messaging.EXCHANGE, true, false);
    }

    @Bean
    Declarables paymentQueues(TopicExchange eventsExchange) {
        return QueueDeclarations.bindWithDlq(
                eventsExchange,
                Messaging.PAYMENT_INVENTORY_RESERVED_QUEUE,
                Messaging.INVENTORY_RESERVED
        );
    }
}
