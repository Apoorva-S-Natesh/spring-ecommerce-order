package ecommerce.service

import ecommerce.dto.analytics.ActiveUserAnalytics
import ecommerce.dto.analytics.TopProductAnalytics
import ecommerce.repository.CartStatisticsRepository
import org.springframework.stereotype.Service

@Service
class CartStatisticsService(
    private val cartStatisticsRepository: CartStatisticsRepository,
) {
    fun findTop5MostAddedProductsLast30Days(): List<TopProductAnalytics> {
        return cartStatisticsRepository.findTop5MostAddedProductsLast30Days()
    }

    fun findMembersActiveInLast7Days(): List<ActiveUserAnalytics> {
        return cartStatisticsRepository.findMembersActiveInLast7Days()
    }
}
