package ecommerce.model

enum class PaymentStatus(val stripeStatus: String) {
    SUCCEEDED("succeeded"),
    REQUIRES_ACTION("requires_action"),
    REQUIRES_PAYMENT_METHOD("requires_payment_method"),
    CANCELED("canceled"),
    PROCESSING("processing"),
    REQUIRES_CONFIRMATION("requires_confirmation"),
    UNKNOWN("unknown"),
    ;

    companion object {
        fun fromStripeStatus(status: String?): PaymentStatus {
            return entries.find { it.stripeStatus == status } ?: UNKNOWN
        }
    }
}
