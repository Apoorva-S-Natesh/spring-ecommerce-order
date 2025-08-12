package ecommerce.stripe

enum class DeclineCode(val stripeCode: String, val userMessage: String) {
    GENERIC_DECLINE(
        "generic_decline",
        "Your card was declined for an unknown reason. Please try another card or contact your bank.",
    ),
    INSUFFICIENT_FUNDS("insufficient_funds", "Your card has insufficient funds. Please add funds or use another card."),
    LOST_CARD("lost_card", "Your card was declined as it is reported lost. Please use another card."),
    STOLEN_CARD("stolen_card", "Your card was declined as it is reported stolen. Please use another card."),
    EXPIRED_CARD("expired_card", "Your card has expired. Please use a valid card."),
    INCORRECT_CVC("incorrect_cvc", "The CVC code provided is incorrect. Please check and try again."),
    PROCESSING_ERROR("processing_error", "A processing error occurred. Please try again later."),
    CARD_VELOCITY_EXCEEDED(
        "card_velocity_exceeded",
        "Your card was declined due to exceeding transaction limits. Please try again later or use another card.",
    ),
    UNKNOWN("unknown", "An unexpected error occurred during payment. Please try again or contact support."),
    ;

    companion object {
        fun fromStripeCode(code: String?): DeclineCode {
            return entries.find { it.stripeCode == code } ?: UNKNOWN
        }
    }
}
