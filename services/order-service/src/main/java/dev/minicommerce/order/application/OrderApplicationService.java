package dev.minicommerce.order.application;

import dev.minicommerce.events.InventoryRejectedEvent;
import dev.minicommerce.events.InventoryReservedEvent;
import dev.minicommerce.events.Messaging;
import dev.minicommerce.events.OrderCreatedEvent;
import dev.minicommerce.events.PaymentApprovedEvent;
import dev.minicommerce.events.PaymentFailedEvent;
import dev.minicommerce.messaging.EventPublisher;
import dev.minicommerce.order.api.CreateOrderRequest;
import dev.minicommerce.order.api.OrderNotFoundException;
import dev.minicommerce.order.api.OrderResponse;
import dev.minicommerce.order.domain.CustomerOrder;
import dev.minicommerce.order.domain.OrderRepository;
import dev.minicommerce.order.domain.OrderStatus;
import dev.minicommerce.order.domain.ProcessedEvent;
import dev.minicommerce.order.domain.ProcessedEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderApplicationService {

    private final OrderRepository orders;
    private final ProcessedEventRepository processedEvents;
    private final org.springframework.context.ApplicationEventPublisher applicationEvents;
    private final EventPublisher eventPublisher;

    public OrderApplicationService(
            OrderRepository orders,
            ProcessedEventRepository processedEvents,
            org.springframework.context.ApplicationEventPublisher applicationEvents,
            EventPublisher eventPublisher
    ) {
        this.orders = orders;
        this.processedEvents = processedEvents;
        this.applicationEvents = applicationEvents;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        CustomerOrder order = CustomerOrder.pending(request.sku(), request.quantity(), request.amount());
        orders.save(order);
        applicationEvents.publishEvent(new OrderCreatedEvent(
                UUID.randomUUID(),
                order.getId(),
                order.getSku(),
                order.getQuantity(),
                order.getAmount(),
                Instant.now()
        ));
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID id) {
        return orders.findById(id).map(OrderResponse::from).orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list() {
        return orders.findTop20ByOrderByCreatedAtDesc().stream().map(OrderResponse::from).toList();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishCreated(OrderCreatedEvent event) {
        eventPublisher.publish(Messaging.ORDER_CREATED, event, event.eventId());
    }

    @Transactional
    public void onReserved(InventoryReservedEvent event) {
        advance(event.eventId(), event.orderId(), OrderStatus.RESERVED);
    }

    @Transactional
    public void onRejected(InventoryRejectedEvent event) {
        advance(event.eventId(), event.orderId(), OrderStatus.REJECTED);
    }

    @Transactional
    public void onPaid(PaymentApprovedEvent event) {
        advance(event.eventId(), event.orderId(), OrderStatus.PAID);
    }

    @Transactional
    public void onPaymentFailed(PaymentFailedEvent event) {
        advance(event.eventId(), event.orderId(), OrderStatus.PAYMENT_FAILED);
    }

    private void advance(UUID eventId, UUID orderId, OrderStatus status) {
        if (processedEvents.existsById(eventId)) {
            return;
        }
        CustomerOrder order = orders.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("order not found: " + orderId));
        order.advanceTo(status);
        processedEvents.save(new ProcessedEvent(eventId, Instant.now()));
    }
}
