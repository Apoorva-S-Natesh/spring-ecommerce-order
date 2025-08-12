package ecommerce.infrastructure

import ecommerce.dto.PaymentRequest
import ecommerce.exception.StripePaymentException
import ecommerce.stripe.StripeClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class StripeClientTest {
    @Autowired
    private lateinit var stripeClient: StripeClient

    @Test
    fun `should create checkoutSession with valid response`() {
        val actual =
            stripeClient.createCheckoutSession(
                PaymentRequest(
                    amount = 1000L,
                    currency = "USD",
                    paymentMethod = "pm_card_visa",
                ),
            )
        println(actual)
        assertNotNull(actual)
        assertThat(actual.id).isNotNull()
        assertThat(actual.amount).isEqualTo(1000L)
        assertThat(actual.currency).isEqualTo("usd")
        assertThat(actual.status).isIn("succeeded", "requires_payment_method", "requires_action")
        assertThat(actual.declineCode).isNull()
    }

    @Test
    fun `should throw Exception with generic decline`() {
        val paymentRequest =
            PaymentRequest(
                amount = 1000L,
                currency = "USD",
                paymentMethod = "pm_card_visa_chargeDeclined",
            )
        val exception =
            assertThrows<StripePaymentException> {
                stripeClient.createCheckoutSession(paymentRequest)
            }
        assertThat(exception.declineCode).isEqualTo("generic_decline")
        assertThat(exception.message).contains("declined")
    }

    @Test
    fun `should throw Exception with radar block`() {
        val paymentRequest =
            PaymentRequest(
                amount = 1000L,
                currency = "USD",
                paymentMethod = "pm_card_radarBlock",
            )

        val exception =
            assertThrows<StripePaymentException> {
                stripeClient.createCheckoutSession(paymentRequest)
            }
        assertThat(exception.declineCode).isEqualTo("fraudulent")
        assertThat(exception.message).contains("Your card was declined.")
    }

    @Test
    fun `should throw Exception for negative amount`() {
        val request =
            PaymentRequest(
                amount = -1000L,
                currency = "USD",
                paymentMethod = "pm_card_visa",
            )

        val exception =
            assertThrows<StripePaymentException> {
                stripeClient.createCheckoutSession(request)
            }
        assertThat(exception.message).isEqualTo("This value must be greater than or equal to 1.")
    }

    @Test
    fun `should throw Exception for blank currency`() {
        val request =
            PaymentRequest(
                amount = 1000L,
                currency = "",
                paymentMethod = "pm_card_visa",
            )

        val exception =
            assertThrows<StripePaymentException> {
                stripeClient.createCheckoutSession(request)
            }
        assertThat(exception.message).contains("Invalid currency:")
    }

    @Test
    fun `should throw StripePaymentException for invalid currency`() {
        val request =
            PaymentRequest(
                amount = 1000L,
                currency = "INVALID",
                paymentMethod = "pm_card_visa",
            )

        val exception =
            assertThrows<StripePaymentException> {
                stripeClient.createCheckoutSession(request)
            }
        assertThat(exception.message).contains("Invalid currency")
        assertThat(exception.declineCode).isNull()
    }
}
