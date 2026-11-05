package dev.minicommerce.messaging;

import dev.minicommerce.events.Messaging;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;

import java.util.ArrayList;
import java.util.List;

public final class QueueDeclarations {

    private QueueDeclarations() {
    }

    public static Declarables bindWithDlq(TopicExchange exchange, String queueName, String routingKey) {
        String dlqName = queueName + ".dlq";
        Queue queue = QueueBuilder.durable(queueName)
                .deadLetterExchange(Messaging.DLX)
                .deadLetterRoutingKey(dlqName)
                .build();
        Queue deadLetterQueue = QueueBuilder.durable(dlqName).build();
        DirectExchange deadLetterExchange = new DirectExchange(Messaging.DLX, true, false);
        return new Declarables(
                queue,
                deadLetterQueue,
                deadLetterExchange,
                BindingBuilder.bind(queue).to(exchange).with(routingKey),
                BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(dlqName)
        );
    }

    public static Declarables concat(Declarables... parts) {
        List<Declarable> all = new ArrayList<>();
        for (Declarables part : parts) {
            all.addAll(part.getDeclarables());
        }
        return new Declarables(all);
    }
}
