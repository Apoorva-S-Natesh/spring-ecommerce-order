package ecommerce.controller

import ecommerce.dto.analytics.ActiveUserAnalytics
import ecommerce.dto.analytics.TopProductAnalytics
import ecommerce.repository.AnalyticsRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/admin/analytics")
@RestController
class AnalyticsController(
    private val analyticsRepository: AnalyticsRepository,
) {
    @GetMapping("/top-products")
    fun getTopProducts(): List<TopProductAnalytics> {
        return analyticsRepository.findTop5MostAddedProductsLast30Days()
    }

    @GetMapping("/active-users")
    fun getActiveUsers(): List<ActiveUserAnalytics> {
        return analyticsRepository.findMembersActiveInLast7Days()
    }
}
