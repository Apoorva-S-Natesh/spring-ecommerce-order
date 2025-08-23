package ecommerce.stripe

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ecommerce.dto.PaymentRequest
import ecommerce.dto.PaymentResponse
import ecommerce.dto.StripeErrorInfo
import ecommerce.dto.StripePaymentIntent
import ecommerce.exception.StripePaymentException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import kotlin.jvm.java

@Component
class StripeClient(
    @Value("\${stripe.secret-key}")
    private val stripeKey: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient: RestClient

    init {
        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(5000)
        factory.setReadTimeout(5000)
        this.restClient =
            RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://api.stripe.com")
                .defaultHeaders { it.setBasicAuth(stripeKey) }
                .build()
    }

    private val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun createCheckoutSession(req: PaymentRequest): PaymentResponse {
        val body =
            listOf(
                "amount=${req.amount}",
                "currency=${req.currency}",
                "payment_method=${req.paymentMethod}",
                "confirm=true",
                "automatic_payment_methods[enabled]=true",
                "automatic_payment_methods[allow_redirects]=never",
            ).joinToString("&")

        return executeStripeRequest(url = "https://api.stripe.com/v1/payment_intents", body = body)
    }

    private fun executeStripeRequest(
        url: String,
        body: String,
    ): PaymentResponse {
        try {
            val resp =
                restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $stripeKey")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toEntity(String::class.java)

            val paymentIntent = mapper.readValue(resp.body, StripePaymentIntent::class.java)

            return PaymentResponse(
                id = paymentIntent.id,
                amount = paymentIntent.amount,
                status = paymentIntent.status,
                paymentMethod = paymentIntent.payment_method,
                currency = paymentIntent.currency,
                declineCode = paymentIntent.last_payment_error?.decline_code,
            )
        } catch (ex: RestClientResponseException) {
            val info = parseStripeError(ex.responseBodyAsString)
            log.error(
                "Stripe API error: status={} code={} declineCode={} msg={}",
                ex.statusCode.value(),
                info.code,
                info.declineCode,
                info.message,
            )
            throw StripePaymentException(
                message = info.message,
                cause = ex,
                declineCode = info.declineCode,
            )
        } catch (ex: Exception) {
            log.error("Unexpected error calling Stripe: {}", ex.message, ex)
            throw StripePaymentException("Unexpected Stripe error: ${ex.message}", ex.message)
        }
    }

    private fun parseStripeError(json: String?): StripeErrorInfo {
        return try {
            val root: JsonNode = mapper.readTree(json ?: "{}")
            val err = root.path("error")
            val message = err.path("message").asText("Stripe error")
            val code = err.path("code").takeIf { it.isTextual }?.asText()
            val decline = err.path("decline_code").takeIf { it.isTextual }?.asText()
            StripeErrorInfo(message = message, code = code, declineCode = decline)
        } catch (e: Exception) {
            StripeErrorInfo(message = "Unable to parse Stripe error: ${e.message}")
        }
    }
}
