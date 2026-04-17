package net.payrdr.mobile.payment.sdk.gpay

import androidx.test.filters.SmallTest
import com.google.android.gms.wallet.WalletConstants
import io.kotest.matchers.shouldBe
import org.junit.Test

@SmallTest
class GooglePayUtilsTest {

    // ---- getEnvironment ----

    @Test
    fun shouldReturnTestEnvironmentWhenIsTestTrue() {
        GooglePayUtils.getEnvironment(isTest = true) shouldBe WalletConstants.ENVIRONMENT_TEST
    }

    @Test
    fun shouldReturnProductionEnvironmentWhenIsTestFalse() {
        GooglePayUtils.getEnvironment(isTest = false) shouldBe WalletConstants.ENVIRONMENT_PRODUCTION
    }

    // ---- getIsReadyToPayJson ----

    @Test
    fun shouldBuildIsReadyToPayJsonWithBaseCardPaymentMethod() {
        val json = GooglePayUtils.getIsReadyToPayJson()

        val methods = json.getJSONArray("allowedPaymentMethods")
        methods.length() shouldBe 1

        val method = methods.getJSONObject(0)
        method.getString("type") shouldBe "CARD"

        val params = method.getJSONObject("parameters")
        params.getBoolean("billingAddressRequired") shouldBe true
        params.getJSONObject("billingAddressParameters").getString("format") shouldBe "FULL"

        val authMethods = params.getJSONArray("allowedAuthMethods")
        val authList = (0 until authMethods.length()).map { authMethods.getString(it) }
        authList shouldBe listOf("PAN_ONLY", "CRYPTOGRAM_3DS")

        val networks = params.getJSONArray("allowedCardNetworks")
        val networkList = (0 until networks.length()).map { networks.getString(it) }
        networkList shouldBe listOf("AMEX", "DISCOVER", "INTERAC", "JCB", "MASTERCARD", "VISA")
    }

    @Test
    fun shouldBuildFreshJsonOnEachCall() {
        val first = GooglePayUtils.getIsReadyToPayJson()
        val second = GooglePayUtils.getIsReadyToPayJson()

        // Different JSONObject instances so callers can safely mutate either without side effects.
        (first === second) shouldBe false
        first.toString() shouldBe second.toString()
    }

    // ---- allowedPaymentMethods ----

    @Test
    fun shouldIncludeTokenizationSpecificationInAllowedPaymentMethods() {
        val spec = TokenizationSpecification().apply {
            type = GoogleTokenizationSpecificationType.PAYMENT_GATEWAY
            parameters = TokenizationSpecificationParameters().apply {
                gateway = "my-gateway"
                gatewayMerchantId = "merchant-42"
            }
        }

        val methods = GooglePayUtils.allowedPaymentMethods(spec)

        methods.length() shouldBe 1
        val method = methods.getJSONObject(0)
        method.getString("type") shouldBe "CARD"

        val tokenization = method.getJSONObject("tokenizationSpecification")
        tokenization.getString("type") shouldBe
            GoogleTokenizationSpecificationType.PAYMENT_GATEWAY.value

        val tokenizationParams = tokenization.getJSONObject("parameters")
        tokenizationParams.getString("gateway") shouldBe "my-gateway"
        tokenizationParams.getString("gatewayMerchantId") shouldBe "merchant-42"
    }

    @Test
    fun shouldKeepBaseCardPaymentMethodShapeWhenAddingTokenization() {
        val spec = TokenizationSpecification()

        val methods = GooglePayUtils.allowedPaymentMethods(spec)
        val method = methods.getJSONObject(0)
        val params = method.getJSONObject("parameters")

        params.getBoolean("billingAddressRequired") shouldBe true
        params.getJSONObject("billingAddressParameters").getString("format") shouldBe "FULL"
        params.has("allowedAuthMethods") shouldBe true
        params.has("allowedCardNetworks") shouldBe true
    }
}
