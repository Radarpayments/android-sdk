package net.payrdr.mobile.payment.sdk.gpay

import android.Manifest
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.form.gpay.AllowedPaymentMethods.Companion.allowedPaymentMethodsCreate
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayAuthMethod.CRYPTOGRAM_3DS
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayAuthMethod.PAN_ONLY
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayCardNetwork.AMEX
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayCardNetwork.DISCOVER
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayCardNetwork.INTERAC
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayCardNetwork.JCB
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayCardNetwork.MASTERCARD
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayCardNetwork.VISA
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayCheckoutOption.COMPLETE_IMMEDIATE_PURCHASE
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayPaymentDataRequest.Companion.paymentDataRequestCreate
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayPaymentMethod.CARD
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayTotalPriceStatus
import net.payrdr.mobile.payment.sdk.form.gpay.GoogleTokenizationSpecificationType.PAYMENT_GATEWAY
import net.payrdr.mobile.payment.sdk.form.gpay.MerchantInfo.Companion.merchantInfoCreate
import net.payrdr.mobile.payment.sdk.form.gpay.PaymentMethodParameters.Companion.paymentMethodParametersCreate
import net.payrdr.mobile.payment.sdk.form.gpay.TokenizationSpecification.Companion.tokenizationSpecificationCreate
import net.payrdr.mobile.payment.sdk.form.gpay.TokenizationSpecificationParameters.Companion.tokenizationSpecificationParametersCreate
import net.payrdr.mobile.payment.sdk.form.gpay.TransactionInfo.Companion.transactionInfoCreate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.math.BigDecimal

@SmallTest
@RunWith(AllureAndroidJUnit4::class)
class GooglePayPaymentDataRequestTest {

    @get:Rule
    val permissionRule: TestRule =
        GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Test
    @Description("shouldUseDefaultValues")
    @Suppress("MaxLineLength")
    fun shouldUseDefaultValues() {
        val configJson = paymentDataRequestCreate {
            allowedPaymentMethods = allowedPaymentMethodsCreate {
                method {
                    type = CARD
                    parameters = paymentMethodParametersCreate {
                        allowedAuthMethods = mutableSetOf(PAN_ONLY, CRYPTOGRAM_3DS)
                        allowedCardNetworks =
                            mutableSetOf(AMEX, DISCOVER, INTERAC, JCB, MASTERCARD, VISA)
                    }
                    tokenizationSpecification = tokenizationSpecificationCreate {
                        type = PAYMENT_GATEWAY
                        parameters = tokenizationSpecificationParametersCreate {
                            gateway = "sberbank"
                            gatewayMerchantId = "sbersafe_test"
                        }
                    }
                }
            }
            transactionInfo = transactionInfoCreate {
                totalPrice = BigDecimal.valueOf(1)
                totalPriceStatus = GooglePayTotalPriceStatus.FINAL
                countryCode = "US"
                currencyCode = "USD"
                checkoutOption = COMPLETE_IMMEDIATE_PURCHASE
            }
            merchantInfo = merchantInfoCreate {
                merchantName = "Example Merchant"
                merchantId = "01234567890123456789"
            }
        }.toJson().toString()

        assertEquals(
            """
                {"apiVersion":2,"apiVersionMinor":0,"allowedPaymentMethods":[{"type":"CARD","parameters":{"allowedAuthMethods":["PAN_ONLY","CRYPTOGRAM_3DS"],"allowedCardNetworks":["AMEX","DISCOVER","INTERAC","JCB","MASTERCARD","VISA"]},"tokenizationSpecification":{"type":"PAYMENT_GATEWAY","parameters":{"gateway":"sberbank","gatewayMerchantId":"sbersafe_test"}}}],"transactionInfo":{"totalPrice":"1","totalPriceStatus":"FINAL","countryCode":"US","currencyCode":"USD","checkoutOption":"COMPLETE_IMMEDIATE_PURCHASE"},"merchantInfo":{"merchantId":"01234567890123456789","merchantName":"Example Merchant"}}
            """.trimIndent(),
            configJson
        )
    }
}
