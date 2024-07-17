package net.payrdr.mobile.payment.sdk.core

import android.Manifest
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule.grant
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.core.model.BindingInstantParams
import net.payrdr.mobile.payment.sdk.core.model.BindingParams
import net.payrdr.mobile.payment.sdk.core.model.CardInstantParams
import net.payrdr.mobile.payment.sdk.core.model.CardParams
import net.payrdr.mobile.payment.sdk.core.model.NewPaymentMethodCardParams
import net.payrdr.mobile.payment.sdk.core.model.NewPaymentMethodStoredCardParams
import net.payrdr.mobile.payment.sdk.core.model.ParamField
import net.payrdr.mobile.payment.sdk.core.model.SDKCoreConfig
import net.payrdr.mobile.payment.sdk.core.validation.ValidationCodes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@SmallTest
@Suppress("LargeClass")
class SDKCoreTest {

    @get:Rule
    val permissionRule: TestRule = grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Suppress("MaxLineLength")
    private val testPubKey =
        "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAij/G3JVV3TqYCFZTPmwi4JduQMsZ2HcFLBBA9fYAvApv3FtA+zKdUGgKh/OPbtpsxe1C57gIaRclbzMoafTb0eOdj+jqSEJMlVJYSiZ8Hn6g67evhu9wXh5ZKBQ1RUpqL36LbhYnIrP+TEGR/VyjbC6QTfaktcRfa8zRqJczHFsyWxnlfwKLfqKz5wSqXkShcrwcfRJCyDRjZX6OFUECHsWVK3WMcOV3WZREwbCkh/o5R5Vl6xoyLvSqVEKQiHupJcZu9UEOJiP3yNCn9YPgyFs2vrCeg6qxDPFnCfetcDCLjjLenGF7VyZzBJ9G2NP3k/mNVtD8Kl7lpiurwY7EZwIDAQAB-----END PUBLIC KEY-----"

    private lateinit var sdkCore: SDKCore

    @Before
    fun setUp() {
        sdkCore = SDKCore(context = InstrumentationRegistry.getInstrumentation().context)
    }

    @Test
    @Description("shouldGenerateNewCardPayment")
    fun shouldGenerateNewCardPayment() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldGenerateNewCardPaymentWithNewPaymentMethod")
    fun shouldGenerateNewCardPaymentWithNewPaymentMethod() {
        val params = NewPaymentMethodCardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldGenerateNewCardPaymentWithoutCardHolder")
    fun shouldGenerateNewCardPaymentWithoutCardHolder() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = null,
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldGenerateNewCardPaymentWithoutCardHolderWithNewPaymentMethod")
    fun shouldGenerateNewCardPaymentWithoutCardHolderWithNewPaymentMethod() {
        val params = NewPaymentMethodCardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = null,
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldGenerateNewCardPaymentWithInvalidSymbolsInCardHolder")
    fun shouldGenerateWithCardWithInvalidSymbolsInCardHolder() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "4554Pav",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalidFormat, result.errors[ParamField.CARDHOLDER])
    }

    @Test
    @Description("shouldGenerateNewCardPaymentWithInvalidSymbolsInCardHolderWithNewPaymentMethod")
    fun shouldGenerateNewCardPaymentWithInvalidSymbolsInCardHolderWithNewPaymentMethod() {
        val params = NewPaymentMethodCardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "4554Pav",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalidFormat, result.errors[ParamField.CARDHOLDER])
    }

    @Test
    @Description("shouldGenerateNewCardPaymentWithMaxLengthInCardHolder")
    fun shouldGenerateNewCardPaymentWithMaxLengthInCardHolder() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "G".repeat(31),
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.CARDHOLDER])
    }

    @Test
    @Description("shouldGenerateNewCardPaymentWithMaxLengthInCardHolderWithNewPaymentMethod")
    fun shouldGenerateNewCardPaymentWithMaxLengthInCardHolderWithNewPaymentMethod() {
        val params = NewPaymentMethodCardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "G".repeat(31),
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.CARDHOLDER])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyMdOrder")
    fun shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyMdOrder() {
        val params = CardParams(
            mdOrder = "",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.MD_ORDER])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyPan")
    fun shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyPan() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.PAN])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyPanWithNewPaymentMethod")
    fun shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyPanWithNewPaymentMethod() {
        val params = NewPaymentMethodCardParams(
            pan = "",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.PAN])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyCVC")
    fun shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyCVC() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.CVC])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyCVCWithNewPaymentMethod")
    fun shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyCVCWithNewPaymentMethod() {
        val params = NewPaymentMethodCardParams(
            pan = "5391119268214792",
            cvc = "",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.CVC])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyExpiry")
    fun shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyExpiry() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.EXPIRY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyExpiryWithNewPaymentMethod")
    fun shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyExpiryWithNewPaymentMethod() {
        val params = NewPaymentMethodCardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.EXPIRY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyPubKey")
    fun shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyPubKey() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = ""
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyPubKeyWithNewPaymentMethod")
    fun shouldReturnErrorWhileGenerateNewCardPaymentWithEmptyPubKeyWithNewPaymentMethod() {
        val params = NewPaymentMethodCardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = ""
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateNewCardPaymentWithInvalidPan")
    fun shouldReturnErrorWhileGenerateNewCardPaymentWithInvalidPan() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5INVALID19268PAN14792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalidFormat, result.errors[ParamField.PAN])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateNewCardPaymentWithInvalidPanWithNewPaymentMethod")
    fun shouldReturnErrorWhileGenerateNewCardPaymentWithInvalidPanWithNewPaymentMethod() {
        val params = NewPaymentMethodCardParams(
            pan = "5INVALID19268PAN14792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalidFormat, result.errors[ParamField.PAN])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateNewCardPaymentWithInvalidCVC")
    fun shouldReturnErrorWhileGenerateNewCardPaymentWithInvalidCVCW() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "1AA",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.CVC])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateNewCardPaymentWithInvalidCVCWithNewPaymentMethod")
    fun shouldReturnErrorWhileGenerateNewCardPaymentWithInvalidCVCWithNewPaymentMethod() {
        val params = NewPaymentMethodCardParams(
            pan = "5391119268214792",
            cvc = "1AA",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.CVC])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateNewCardPaymentWithInvalidExpiry")
    fun shouldReturnErrorWhileGenerateNewCardPaymentWithInvalidExpiry() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "DDD",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalidFormat, result.errors[ParamField.EXPIRY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateNewCardPaymentWithInvalidExpiryWithNewPaymentMethod")
    fun shouldReturnErrorWhileGenerateNewCardPaymentWithInvalidExpiryWithNewPaymentMethod() {
        val params = NewPaymentMethodCardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "DDD",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalidFormat, result.errors[ParamField.EXPIRY])
    }

    @Test
    @Description("shouldNotReturnErrorWhileGenerateNewCardPaymentWithCardDateExpiry")
    fun shouldNotReturnErrorWhileGenerateNewCardPaymentWithCardDateExpiry() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/35",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertNotEquals(null, result.token)
        assertEquals(true, result.errors.isEmpty())
    }

    @Test
    @Description("shouldNotReturnErrorWhileGenerateNewCardPaymentWithCardDateExpiryWithNewPaymentMethod")
    fun shouldNotReturnErrorWhileGenerateNewCardPaymentWithCardDateExpiryWithNewPaymentMethod() {
        val params = NewPaymentMethodCardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/35",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertNotEquals(null, result.token)
        assertEquals(true, result.errors.isEmpty())
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateNewCardPaymentWithInvalidPubKey")
    fun shouldReturnErrorWhileGenerateNewCardPaymentWithInvalidPubKey() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = "INVALIDPUBKEY"
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateNewCardPaymentWithInvalidPubKeyWithNewPaymentMethod")
    fun shouldReturnErrorWhileGenerateNewCardPaymentWithInvalidPubKeyWithNewPaymentMethod() {
        val params = NewPaymentMethodCardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = "INVALIDPUBKEY"
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldGenerateStoredPayment")
    fun shouldGenerateStoredPayment() {
        val params = BindingParams(
            mdOrder = "39ce26e1-5fd0-4784-9e6c-25c9f2c2d09e",
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "123",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldGenerateStoredPaymentWithNewPaymentMethod")
    fun shouldGenerateStoredPaymentWithNewPaymentMethod() {
        val params = NewPaymentMethodStoredCardParams(
            storedPaymentId = "pm_QRiwYPoAGtbRrETy1uP6RovMnsF2W3aA2xbeRhG8F4Sf6b9vY",
            cvc = "123",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldGenerateStoredPaymentWithoutCVC")
    fun shouldGenerateStoredPaymentWithoutCVC() {
        val params = BindingParams(
            mdOrder = "39ce26e1-5fd0-4784-9e6c-25c9f2c2d09e",
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = null,
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldGenerateStoredPaymentWithoutCVCWithNewPaymentMehod")
    fun shouldGenerateStoredPaymentWithoutCVCWithNewPaymentMehod() {
        val params = NewPaymentMethodStoredCardParams(
            storedPaymentId = "pm_QRiwYPoAGtbRrETy1uP6RovMnsF2W3aA2xbeRhG8F4Sf6b9vY",
            cvc = null,
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldReturnErrorGenerateStoredPaymentWithEmptyMdOrder")
    fun shouldReturnErrorGenerateStoredPaymentWithEmptyMdOrder() {
        val params = BindingParams(
            mdOrder = "",
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "123",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.MD_ORDER])
    }

    @Test
    @Description("shouldReturnErrorGenerateStoredPaymentWithEmptyBindingID")
    fun shouldReturnErrorGenerateStoredPaymentWithEmptyBindingID() {
        val params = BindingParams(
            mdOrder = "39ce26e1-5fd0-4784-9e6c-25c9f2c2d09e",
            bindingID = "",
            cvc = "123",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.BINDING_ID])
    }

    @Test
    @Description("shouldReturnErrorGenerateStoredPaymentWithNewPaymentMethodWithEmptyStoredPaymentID")
    fun shouldReturnErrorGenerateStoredPaymentWithNewPaymentMethodWithEmptyStoredPaymentID() {
        val params = NewPaymentMethodStoredCardParams(
            storedPaymentId = "",
            cvc = "123",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.STORED_PAYMENT_ID])
    }

    @Test
    @Description("shouldReturnErrorGenerateStoredPaymentWithEmptyPubKey")
    fun shouldReturnErrorGenerateStoredPaymentWithEmptyPubKey() {
        val params = BindingParams(
            mdOrder = "39ce26e1-5fd0-4784-9e6c-25c9f2c2d09e",
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "123",
            pubKey = ""
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldReturnErrorGenerateStoredPaymentWithEmptyPubKeyWithNewPaymentMethod")
    fun shouldReturnErrorGenerateStoredPaymentWithEmptyPubKeyWithNewPaymentMethod() {
        val params = NewPaymentMethodStoredCardParams(
            storedPaymentId = "pm_QRiwYPoAGtbRrETy1uP6RovMnsF2W3aA2xbeRhG8F4Sf6b9vY",
            cvc = "123",
            pubKey = ""
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldReturnErrorGenerateStoredPaymentWithInvalidCVC")
    fun shouldReturnErrorGenerateStoredPaymentWithInvalidCVC() {
        val params = BindingParams(
            mdOrder = "39ce26e1-5fd0-4784-9e6c-25c9f2c2d09e",
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "aaD",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.CVC])
    }

    @Test
    @Description("shouldReturnErrorGenerateStoredPaymentWithInvalidCVCWithNewPaymentMethod")
    fun shouldReturnErrorGenerateStoredPaymentWithInvalidCVCWithNewPaymentMethod() {
        val params = NewPaymentMethodStoredCardParams(
            storedPaymentId = "pm_QRiwYPoAGtbRrETy1uP6RovMnsF2W3aA2xbeRhG8F4Sf6b9vY",
            cvc = "aaD",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.CVC])
    }

    @Test
    @Description("shouldReturnErrorGenerateStoredPaymentWithInvalidPubKey")
    fun shouldReturnErrorGenerateStoredPaymentWithInvalidPubKey() {
        val params = BindingParams(
            mdOrder = "39ce26e1-5fd0-4784-9e6c-25c9f2c2d09e",
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "123",
            pubKey = "INVALIDPUBKEY"
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldReturnErrorGenerateStoredPaymentWithInvalidPubKeyWithNewPaymentMethod")
    fun shouldReturnErrorGenerateStoredPaymentWithInvalidPubKeyWithNewPaymentMethod() {
        val params = NewPaymentMethodStoredCardParams(
            storedPaymentId = "pm_QRiwYPoAGtbRrETy1uP6RovMnsF2W3aA2xbeRhG8F4Sf6b9vY",
            cvc = "123",
            pubKey = "INVALIDPUBKEY"
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldGenerateInstantWithCard")
    fun shouldGenerateInstantWithCard() {
        val params = CardInstantParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldGenerateInstantWithCardWithoutCardHolder")
    fun shouldGenerateInstantWithCardWithoutCardHolder() {
        val params = CardInstantParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = null,
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldGenerateInstantWithCardWithInvalidSymbolsInCardHolder")
    fun shouldGenerateInstantWithCardWithInvalidSymbolsInCardHolder() {
        val params = CardInstantParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "4554Pav",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalidFormat, result.errors[ParamField.CARDHOLDER])
    }

    @Test
    @Description("shouldGenerateInstantWithCardWithMaxLengthInCardHolder")
    fun shouldGenerateInstantWithCardWithMaxLengthInCardHolder() {
        val params = CardInstantParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "G".repeat(31),
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.CARDHOLDER])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstantWithCardWithEmptyPan")
    fun shouldReturnErrorWhileGenerateInstantWithCardWithEmptyPan() {
        val params = CardInstantParams(
            pan = "",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.PAN])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstantWithCardWithEmptyCVC")
    fun shouldReturnErrorWhileGenerateInstantWithCardWithEmptyCVC() {
        val params = CardInstantParams(
            pan = "5391119268214792",
            cvc = "",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.CVC])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstantWithCardWithEmptyExpiry")
    fun shouldReturnErrorWhileGenerateInstantWithCardWithEmptyExpiry() {
        val params = CardInstantParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.EXPIRY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstantWithCardWithEmptyPubKey")
    fun shouldReturnErrorWhileGenerateInstantWithCardWithEmptyPubKey() {
        val params = CardInstantParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = ""
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstantWithCardWithInvalidPan")
    fun shouldReturnErrorWhileGenerateInstantWithCardWithInvalidPan() {
        val params = CardInstantParams(
            pan = "5INVALID19268PAN14792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalidFormat, result.errors[ParamField.PAN])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstantWithCardWithInvalidCVC")
    fun shouldReturnErrorWhileGenerateInstantWithCardWithInvalidCVC() {
        val params = CardInstantParams(
            pan = "5391119268214792",
            cvc = "1AA",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.CVC])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstantWithCardWithInvalidExpiry")
    fun shouldReturnErrorWhileGenerateInstantWithCardWithInvalidExpiry() {
        val params = CardInstantParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "DDD",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalidFormat, result.errors[ParamField.EXPIRY])
    }

    @Test
    @Description("shouldNotReturnErrorWhileGenerateInstantWithCardWithOutDateExpiry")
    fun shouldNotReturnErrorWhileGenerateInstantWithCardWithOutDateExpiry() {
        val params = CardInstantParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/15",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertNotEquals(null, result.token)
        assertEquals(true, result.errors.isEmpty())
    }

    @Test
    @Description("shouldNotReturnErrorWhileGenerateInstantWithCardWithMaxOutDateExpiry")
    fun shouldNotReturnErrorWhileGenerateInstantWithCardWithMaxOutDateExpiry() {
        val params = CardInstantParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/35",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertNotEquals(null, result.token)
        assertEquals(true, result.errors.isEmpty())
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstantWithCardWithInvalidPubKey")
    fun shouldReturnErrorWhileGenerateInstantWithCardWithInvalidPubKey() {
        val params = CardInstantParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = "INVALIDPUBKEY"
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldGenerateInstantWithBinding")
    fun shouldGenerateInstantWithBinding() {
        val params = BindingInstantParams(
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "123",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldGenerateInstantWithBindingWithoutCVC")
    fun shouldGenerateInstantWithBindingWithoutCVC() {
        val params = BindingInstantParams(
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = null,
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldReturnErrorGenerateInstantWithBindingWithEmptyBindingID")
    fun shouldReturnErrorGenerateInstantWithBindingWithEmptyBindingID() {
        val params = BindingInstantParams(
            bindingID = "",
            cvc = "123",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.BINDING_ID])
    }

    @Test
    @Description("shouldReturnErrorGenerateInstantWithBindingWithEmptyPubKey")
    fun shouldReturnErrorGenerateInstantWithBindingWithEmptyPubKey() {
        val params = BindingInstantParams(
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "123",
            pubKey = ""
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldReturnErrorGenerateInstantWithBindingWithInvalidCVC")
    fun shouldReturnErrorGenerateInstantWithBindingWithInvalidCVC() {
        val params = BindingInstantParams(
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "aaD",
            pubKey = testPubKey
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.CVC])
    }

    @Test
    @Description("shouldReturnErrorGenerateInstantWithBindingWithInvalidPubKey")
    fun shouldReturnErrorGenerateInstantWithBindingWithInvalidPubKey() {
        val params = BindingInstantParams(
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "123",
            pubKey = "INVALIDPUBKEY"
        )

        val sdkCoreConfig = SDKCoreConfig(paymentCardParams = params)
        val result = sdkCore.generateWithConfig(sdkCoreConfig)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.PUB_KEY])
    }
}
