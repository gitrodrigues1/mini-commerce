package dev.minicommerce.payment.domain;

import java.util.Optional;

public final class PaymentPolicy {

    public static final String FAILING_SKU = "SKU-FAIL-PAY";

    private PaymentPolicy() {
    }

    public static Optional<String> declineReason(String sku) {
        if (FAILING_SKU.equals(sku)) {
            return Optional.of("simulated decline");
        }
        return Optional.empty();
    }
}
