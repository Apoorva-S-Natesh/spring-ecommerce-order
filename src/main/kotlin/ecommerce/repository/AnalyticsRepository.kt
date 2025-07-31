package ecommerce.repository

import ecommerce.dto.analytics.ActiveUserAnalytics
import ecommerce.dto.analytics.TopProductAnalytics
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

@Repository
class AnalyticsRepository(private val jdbc: JdbcTemplate) {
    private val topProductRowMapper =
        RowMapper<TopProductAnalytics> { rs, _ ->
            TopProductAnalytics(
                productName = rs.getString("productName"),
                addedCount = rs.getInt("addedCount"),
                mostRecentAdded = rs.getTimestamp("mostRecentAdded")?.toLocalDateTime(),
            )
        }

    private val activeUserRowMapper =
        RowMapper<ActiveUserAnalytics> { rs, _ ->
            ActiveUserAnalytics(
                memberId = rs.getLong("memberId"),
                memberName = rs.getString("memberName"),
                memberEmail = rs.getString("memberEmail"),
            )
        }

    fun findTop5MostAddedProductsLast30Days(): List<TopProductAnalytics> {
        val sql =
            """
            SELECT 
                p.name as productName,
                SUM(c.quantity) as addedCount,
                MAX(c.added_at) as mostRecentAdded
            FROM cart c
            JOIN products p ON c.product_id = p.id
            WHERE c.added_at >= DATEADD('DAY', -30, CURRENT_TIMESTAMP)
            GROUP BY c.product_id, p.name
            ORDER BY SUM(c.quantity) DESC, MAX(c.added_at) DESC
            LIMIT 5
            """.trimIndent()

        return try {
            jdbc.query(sql, topProductRowMapper)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun findMembersActiveInLast7Days(): List<ActiveUserAnalytics> {
        val sql =
            """
            SELECT DISTINCT 
                m.id as memberId,
                m.name as memberName,
                m.email as memberEmail
            FROM members m
            WHERE EXISTS (
                SELECT 1 FROM cart c 
                WHERE c.member_id = m.id 
                AND c.added_at >= DATEADD('DAY', -7, CURRENT_TIMESTAMP)
            )
            """.trimIndent()

        return try {
            jdbc.query(sql, activeUserRowMapper)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
