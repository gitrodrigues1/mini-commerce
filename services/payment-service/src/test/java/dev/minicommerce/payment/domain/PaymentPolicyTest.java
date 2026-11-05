package dev.minicommerce.payment.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentPolicyTest {

    @Test
    void failingSkuIsDeclined() {
        assertEquals("simulated decline", PaymentPolicy.declineReason(PaymentPolicy.FAILING_SKU).orElseThrow());
    }

    @Test
    void otherSkusAreApproved() {
        assertTrue(PaymentPolicy.declineReason("BOOK-01").isEmpty());
    }
}
