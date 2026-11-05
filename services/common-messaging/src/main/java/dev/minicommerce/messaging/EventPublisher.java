package dev.minicommerce.messaging;

import dev.minicommerce.events.Messaging;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(String routingKey, Object event, UUID eventId) {
        rabbitTemplate.convertAndSend(Messaging.EXCHANGE, routingKey, event, message -> {
            message.getMessageProperties().setMessageId(eventId.toString());
            return message;
        });
    }
}
