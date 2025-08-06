package ecommerce.controller

import ecommerce.dto.analytics.ActiveUserAnalytics
import ecommerce.dto.analytics.TopProductAnalytics
import ecommerce.service.CartStatisticsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/admin/analytics")
@RestController
class CartStatisticsController(
    private val cartStatisticsService: CartStatisticsService,
) {
    @GetMapping("/top-products")
    fun getTopProducts(): List<TopProductAnalytics> {
        return cartStatisticsService.findTop5MostAddedProductsLast30Days()
    }

    @GetMapping("/active-users")
    fun getActiveUsers(): List<ActiveUserAnalytics> {
        return cartStatisticsService.findMembersActiveInLast7Days()
    }
}
