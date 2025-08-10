package ecommerce.service

import ecommerce.dto.PlaceOrderRequest
import ecommerce.StripeClient
import ecommerce.stripe.StripeProperties
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest

@EnableConfigurationProperties(StripeProperties::class)
@SpringBootTest
class StripeClientTest {
    @Autowired
    private lateinit var stripeClient: StripeClient

    @Test
    fun test1() {
        val request = PlaceOrderRequest(1, 1, 1000.0, "USD", "pm_card_visa")
        val actual = stripeClient.createCheckoutSession(request)
        println(actual)
        Assertions.assertThat(actual?.id).isNotNull()
        Assertions.assertThat(actual?.amount).isNotNull()
    }
}
