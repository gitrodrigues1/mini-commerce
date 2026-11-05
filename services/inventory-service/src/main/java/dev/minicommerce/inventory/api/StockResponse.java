package dev.minicommerce.inventory.api;

import dev.minicommerce.inventory.domain.StockItem;

public record StockResponse(String sku, int availableQty, int reservedQty) {

    public static StockResponse from(StockItem item) {
        return new StockResponse(item.getSku(), item.getAvailableQty(), item.getReservedQty());
    }
}
