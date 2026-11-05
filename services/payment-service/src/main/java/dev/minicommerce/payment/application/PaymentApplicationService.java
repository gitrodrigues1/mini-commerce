package dev.minicommerce.payment.application;

import dev.minicommerce.events.InventoryReservedEvent;
import dev.minicommerce.events.Messaging;
import dev.minicommerce.events.PaymentApprovedEvent;
import dev.minicommerce.events.PaymentFailedEvent;
import dev.minicommerce.messaging.EventPublisher;
import dev.minicommerce.payment.domain.Payment;
import dev.minicommerce.payment.domain.PaymentPolicy;
import dev.minicommerce.payment.domain.PaymentRepository;
import dev.minicommerce.payment.domain.PaymentStatus;
import dev.minicommerce.payment.domain.ProcessedEvent;
import dev.minicommerce.payment.domain.ProcessedEventRepository;
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
public class PaymentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentApplicationService.class);

    private final PaymentRepository payments;
    private final ProcessedEventRepository processedEvents;
    private final ApplicationEventPublisher applicationEvents;
    private final EventPublisher eventPublisher;

    public PaymentApplicationService(
            PaymentRepository payments,
            ProcessedEventRepository processedEvents,
            ApplicationEventPublisher applicationEvents,
            EventPublisher eventPublisher
    ) {
        this.payments = payments;
        this.processedEvents = processedEvents;
        this.applicationEvents = applicationEvents;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void charge(InventoryReservedEvent event) {
        if (processedEvents.existsById(event.eventId()) || payments.existsById(event.orderId())) {
            return;
        }
        var decline = PaymentPolicy.declineReason(event.sku());
        if (decline.isPresent()) {
            log.info("declining payment for order {}", event.orderId());
            payments.save(Payment.decided(
                    event.orderId(), event.sku(), event.amount(), PaymentStatus.DECLINED, decline.get()));
            applicationEvents.publishEvent(new PaymentFailedEvent(
                    UUID.randomUUID(), event.orderId(), decline.get(), Instant.now()));
        } else {
            payments.save(Payment.decided(
                    event.orderId(), event.sku(), event.amount(), PaymentStatus.APPROVED, null));
            applicationEvents.publishEvent(new PaymentApprovedEvent(
                    UUID.randomUUID(), event.orderId(), Instant.now()));
        }
        processedEvents.save(new ProcessedEvent(event.eventId(), Instant.now()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishApproved(PaymentApprovedEvent event) {
        eventPublisher.publish(Messaging.PAYMENT_APPROVED, event, event.eventId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishFailed(PaymentFailedEvent event) {
        eventPublisher.publish(Messaging.PAYMENT_FAILED, event, event.eventId());
    }
}
