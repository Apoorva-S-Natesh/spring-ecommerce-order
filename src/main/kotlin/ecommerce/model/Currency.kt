package ecommerce.model

enum class Currency(val code: String) {
    USD("usd"),
    EUR("eur"),
    GBP("gbp"),
    JPY("jpy"),
    CAD("cad"),
    AUD("aud"),
    UNKNOWN(""),
    ;

    companion object {
        fun fromCode(code: String?): Currency {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
