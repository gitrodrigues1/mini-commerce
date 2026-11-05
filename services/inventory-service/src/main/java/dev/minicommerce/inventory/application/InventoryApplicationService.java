package dev.minicommerce.inventory.application;

import dev.minicommerce.events.InventoryRejectedEvent;
import dev.minicommerce.events.InventoryReservedEvent;
import dev.minicommerce.events.Messaging;
import dev.minicommerce.events.OrderCreatedEvent;
import dev.minicommerce.events.PaymentApprovedEvent;
import dev.minicommerce.events.PaymentFailedEvent;
import dev.minicommerce.inventory.domain.ProcessedEvent;
import dev.minicommerce.inventory.domain.ProcessedEventRepository;
import dev.minicommerce.inventory.domain.Reservation;
import dev.minicommerce.inventory.domain.ReservationRepository;
import dev.minicommerce.inventory.domain.ReservationStatus;
import dev.minicommerce.inventory.domain.StockItem;
import dev.minicommerce.inventory.domain.StockItemRepository;
import dev.minicommerce.messaging.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;

@Service
public class InventoryApplicationService {

    private static final Logger log = LoggerFactory.getLogger(InventoryApplicationService.class);

    private final StockItemRepository stock;
    private final ReservationRepository reservations;
    private final ProcessedEventRepository processedEvents;
    private final ApplicationEventPublisher applicationEvents;
    private final EventPublisher eventPublisher;

    public InventoryApplicationService(
            StockItemRepository stock,
            ReservationRepository reservations,
            ProcessedEventRepository processedEvents,
            ApplicationEventPublisher applicationEvents,
            EventPublisher eventPublisher
    ) {
        this.stock = stock;
        this.reservations = reservations;
        this.processedEvents = processedEvents;
        this.applicationEvents = applicationEvents;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void reserve(OrderCreatedEvent event) {
        if (processedEvents.existsById(event.eventId())) {
            return;
        }
        if (reservations.findById(event.orderId()).isPresent()) {
            processedEvents.save(new ProcessedEvent(event.eventId(), Instant.now()));
            return;
        }

        StockItem item = stock.findBySkuForUpdate(event.sku()).orElse(null);
        if (item == null || !item.canReserve(event.quantity())) {
            String reason = item == null ? "unknown sku" : "insufficient stock";
            log.info("rejecting order {} sku {} reason {}", event.orderId(), event.sku(), reason);
            applicationEvents.publishEvent(new InventoryRejectedEvent(
                    UUID.randomUUID(), event.orderId(), reason, Instant.now()));
        } else {
            item.reserve(event.quantity());
            reservations.save(Reservation.reserved(event.orderId(), event.sku(), event.quantity()));
            applicationEvents.publishEvent(new InventoryReservedEvent(
                    UUID.randomUUID(),
                    event.orderId(),
                    event.sku(),
                    event.quantity(),
                    event.amount(),
                    Instant.now()
            ));
        }
        processedEvents.save(new ProcessedEvent(event.eventId(), Instant.now()));
    }

    @Transactional
    public void release(PaymentFailedEvent event) {
        if (processedEvents.existsById(event.eventId())) {
            return;
        }
        reservations.findById(event.orderId()).ifPresent(reservation -> {
            if (reservation.getStatus() == ReservationStatus.RESERVED) {
                stock.findBySkuForUpdate(reservation.getSku()).ifPresent(item -> item.release(reservation.getQuantity()));
                reservation.release();
            }
        });
        processedEvents.save(new ProcessedEvent(event.eventId(), Instant.now()));
    }

    @Transactional
    public void confirm(PaymentApprovedEvent event) {
        if (processedEvents.existsById(event.eventId())) {
            return;
        }
        reservations.findById(event.orderId()).ifPresent(reservation -> {
            if (reservation.getStatus() == ReservationStatus.RESERVED) {
                stock.findBySkuForUpdate(reservation.getSku()).ifPresent(item -> item.confirm(reservation.getQuantity()));
                reservation.confirm();
            }
        });
        processedEvents.save(new ProcessedEvent(event.eventId(), Instant.now()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishReserved(InventoryReservedEvent event) {
        eventPublisher.publish(Messaging.INVENTORY_RESERVED, event, event.eventId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishRejected(InventoryRejectedEvent event) {
        eventPublisher.publish(Messaging.INVENTORY_REJECTED, event, event.eventId());
    }
}
