package dev.minicommerce.events;

public final class Messaging {

    public static final String EXCHANGE = "minicommerce.events";
    public static final String DLX = "minicommerce.events.dlx";

    public static final String ORDER_CREATED = "order.created";
    public static final String INVENTORY_RESERVED = "inventory.reserved";
    public static final String INVENTORY_REJECTED = "inventory.rejected";
    public static final String PAYMENT_APPROVED = "payment.approved";
    public static final String PAYMENT_FAILED = "payment.failed";

    public static final String INVENTORY_ORDER_CREATED_QUEUE = "inventory.order-created";
    public static final String INVENTORY_PAYMENT_FAILED_QUEUE = "inventory.payment-failed";
    public static final String INVENTORY_PAYMENT_APPROVED_QUEUE = "inventory.payment-approved";
    public static final String PAYMENT_INVENTORY_RESERVED_QUEUE = "payment.inventory-reserved";
    public static final String ORDER_INVENTORY_RESERVED_QUEUE = "order.inventory-reserved";
    public static final String ORDER_INVENTORY_REJECTED_QUEUE = "order.inventory-rejected";
    public static final String ORDER_PAYMENT_APPROVED_QUEUE = "order.payment-approved";
    public static final String ORDER_PAYMENT_FAILED_QUEUE = "order.payment-failed";

    private Messaging() {
    }
}
