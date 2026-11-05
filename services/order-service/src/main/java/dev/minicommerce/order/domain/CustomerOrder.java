package dev.minicommerce.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected CustomerOrder() {
    }

    public static CustomerOrder pending(String sku, int quantity, BigDecimal amount) {
        CustomerOrder order = new CustomerOrder();
        order.id = UUID.randomUUID();
        Instant now = Instant.now();
        order.sku = sku;
        order.quantity = quantity;
        order.amount = amount;
        order.status = OrderStatus.PENDING;
        order.createdAt = now;
        order.updatedAt = now;
        return order;
    }

    public void advanceTo(OrderStatus next) {
        this.status = this.status.advanceTo(next);
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
