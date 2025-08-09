package ecommerce.dto.analytics

import java.time.LocalDateTime

class TopProductAnalytics(
    val productName: String,
    val addedCount: Int,
    val mostRecentAdded: LocalDateTime?,
)
