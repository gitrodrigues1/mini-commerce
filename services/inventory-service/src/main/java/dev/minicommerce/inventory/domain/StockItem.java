package dev.minicommerce.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock_items")
public class StockItem {

    @Id
    private String sku;

    @Column(nullable = false)
    private int availableQty;

    @Column(nullable = false)
    private int reservedQty;

    protected StockItem() {
    }

    public boolean canReserve(int quantity) {
        return availableQty >= quantity;
    }

    public void reserve(int quantity) {
        this.availableQty -= quantity;
        this.reservedQty += quantity;
    }

    public void release(int quantity) {
        this.availableQty += quantity;
        this.reservedQty -= quantity;
    }

    public void confirm(int quantity) {
        this.reservedQty -= quantity;
    }

    public String getSku() {
        return sku;
    }

    public int getAvailableQty() {
        return availableQty;
    }

    public int getReservedQty() {
        return reservedQty;
    }
}
