package ecommerce.model

enum class OrderSortOption(val fieldName: String) {
    ID("id"),
    ORDER_DATE("orderDate"),
    AMOUNT("amount"),
    ;

    companion object {
        fun fromString(field: String): OrderSortOption {
            return entries.find { it.fieldName.equals(field, ignoreCase = true) } ?: ID
        }
    }
}
