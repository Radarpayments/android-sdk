package net.payrdr.mobile.payment.sdk.core

import android.Manifest
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule.grant
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.core.model.BindingParams
import net.payrdr.mobile.payment.sdk.core.model.CardParams
import net.payrdr.mobile.payment.sdk.core.model.ParamField
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
    @Description("shouldGenerateWithCard")
    fun shouldGenerateWithCard() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithCard(params)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldGenerateWithCardWithoutCardHolder")
    fun shouldGenerateWithCardWithoutCardHolder() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = null,
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithCard(params)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldGenerateWithCardWithInvalidSymbolsInCardHolder")
    fun shouldGenerateWithCardWithInvalidSymbolsInCardHolder() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "4554Pav",
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalidFormat, result.errors[ParamField.CARDHOLDER])
    }

    @Test
    @Description("shouldGenerateWithCardWithMaxLengthInCardHolder")
    fun shouldGenerateWithCardWithMaxLengthInCardHolder() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "G".repeat(31),
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.CARDHOLDER])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateWithCardWithEmptyMdOrder")
    fun shouldReturnErrorWhileGenerateWithCardWithEmptyMdOrder() {
        val params = CardParams(
            mdOrder = "",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.MD_ORDER])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateWithCardWithEmptyPan")
    fun shouldReturnErrorWhileGenerateWithCardWithEmptyPan() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.PAN])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateWithCardWithEmptyCVC")
    fun shouldReturnErrorWhileGenerateWithCardWithEmptyCVC() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.CVC])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateWithCardWithEmptyExpiry")
    fun shouldReturnErrorWhileGenerateWithCardWithEmptyExpiry() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.EXPIRY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateWithCardWithEmptyPubKey")
    fun shouldReturnErrorWhileGenerateWithCardWithEmptyPubKey() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = ""
        )

        val result = sdkCore.generateWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateWithCardWithInvalidPan")
    fun shouldReturnErrorWhileGenerateWithCardWithInvalidPan() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5INVALID19268PAN14792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalidFormat, result.errors[ParamField.PAN])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateWithCardWithInvalidCVC")
    fun shouldReturnErrorWhileGenerateWithCardWithInvalidCVC() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "1AA",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.CVC])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateWithCardWithInvalidExpiry")
    fun shouldReturnErrorWhileGenerateWithCardWithInvalidExpiry() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "DDD",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalidFormat, result.errors[ParamField.EXPIRY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateWithCardWithOutDateExpiry")
    fun shouldReturnErrorWhileGenerateWithCardWithOutDateExpiry() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/15",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.EXPIRY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateWithCardWithMaxOutDateExpiry")
    fun shouldReturnErrorWhileGenerateWithCardWithMaxOutDateExpiry() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/35",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.EXPIRY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateWithCardWithInvalidPubKey")
    fun shouldReturnErrorWhileGenerateWithCardWithInvalidPubKey() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = "INVALIDPUBKEY"
        )

        val result = sdkCore.generateWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldGenerateWithBinding")
    fun shouldGenerateWithBinding() {
        val params = BindingParams(
            mdOrder = "39ce26e1-5fd0-4784-9e6c-25c9f2c2d09e",
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "123",
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithBinding(params)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldGenerateWithBindingWithoutCVC")
    fun shouldGenerateWithBindingWithoutCVC() {
        val params = BindingParams(
            mdOrder = "39ce26e1-5fd0-4784-9e6c-25c9f2c2d09e",
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = null,
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithBinding(params)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldReturnErrorGenerateWithBindingWithEmptyMdOrder")
    fun shouldReturnErrorGenerateWithBindingWithEmptyMdOrder() {
        val params = BindingParams(
            mdOrder = "",
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "123",
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithBinding(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.MD_ORDER])
    }

    @Test
    @Description("shouldReturnErrorGenerateWithBindingWithEmptyBindingID")
    fun shouldReturnErrorGenerateWithBindingWithEmptyBindingID() {
        val params = BindingParams(
            mdOrder = "39ce26e1-5fd0-4784-9e6c-25c9f2c2d09e",
            bindingID = "",
            cvc = "123",
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithBinding(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.BINDING_ID])
    }

    @Test
    @Description("shouldReturnErrorGenerateWithBindingWithEmptyPubKey")
    fun shouldReturnErrorGenerateWithBindingWithEmptyPubKey() {
        val params = BindingParams(
            mdOrder = "39ce26e1-5fd0-4784-9e6c-25c9f2c2d09e",
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "123",
            pubKey = ""
        )

        val result = sdkCore.generateWithBinding(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldReturnErrorGenerateWithBindingWithInvalidCVC")
    fun shouldReturnErrorGenerateWithBindingWithInvalidCVC() {
        val params = BindingParams(
            mdOrder = "39ce26e1-5fd0-4784-9e6c-25c9f2c2d09e",
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "aaD",
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithBinding(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.CVC])
    }

    @Test
    @Description("shouldReturnErrorGenerateWithBindingWithInvalidPubKey")
    fun shouldReturnErrorGenerateWithBindingWithInvalidPubKey() {
        val params = BindingParams(
            mdOrder = "39ce26e1-5fd0-4784-9e6c-25c9f2c2d09e",
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "123",
            pubKey = "INVALIDPUBKEY"
        )

        val result = sdkCore.generateWithBinding(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldGenerateInstanceWithCard")
    fun shouldGenerateInstanceWithCard() {
        val params = CardParams(
            mdOrder = "c400b41a-aa3d-43db-8727-ac4ca9e8f701",
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateWithCard(params)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldGenerateInstanceWithCardWithoutCardHolder")
    fun shouldGenerateInstanceWithCardWithoutCardHolder() {
        val params = CardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = null,
            pubKey = testPubKey
        )

        val result = sdkCore.generateInstanceWithCard(params)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldGenerateInstanceWithCardWithInvalidSymbolsInCardHolder")
    fun shouldGenerateInstanceWithCardWithInvalidSymbolsInCardHolder() {
        val params = CardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "4554Pav",
            pubKey = testPubKey
        )

        val result = sdkCore.generateInstanceWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalidFormat, result.errors[ParamField.CARDHOLDER])
    }

    @Test
    @Description("shouldGenerateInstanceWithCardWithMaxLengthInCardHolder")
    fun shouldGenerateInstanceWithCardWithMaxLengthInCardHolder() {
        val params = CardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "G".repeat(31),
            pubKey = testPubKey
        )

        val result = sdkCore.generateInstanceWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.CARDHOLDER])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstanceWithCardWithEmptyPan")
    fun shouldReturnErrorWhileGenerateInstanceWithCardWithEmptyPan() {
        val params = CardParams(
            pan = "",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateInstanceWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.PAN])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstanceWithCardWithEmptyCVC")
    fun shouldReturnErrorWhileGenerateInstanceWithCardWithEmptyCVC() {
        val params = CardParams(
            pan = "5391119268214792",
            cvc = "",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateInstanceWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.CVC])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstanceWithCardWithEmptyExpiry")
    fun shouldReturnErrorWhileGenerateInstanceWithCardWithEmptyExpiry() {
        val params = CardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateInstanceWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.EXPIRY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstanceWithCardWithEmptyPubKey")
    fun shouldReturnErrorWhileGenerateInstanceWithCardWithEmptyPubKey() {
        val params = CardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = ""
        )

        val result = sdkCore.generateInstanceWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstanceWithCardWithInvalidPan")
    fun shouldReturnErrorWhileGenerateInstanceWithCardWithInvalidPan() {
        val params = CardParams(
            pan = "5INVALID19268PAN14792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateInstanceWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalidFormat, result.errors[ParamField.PAN])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstanceWithCardWithInvalidCVC")
    fun shouldReturnErrorWhileGenerateInstanceWithCardWithInvalidCVC() {
        val params = CardParams(
            pan = "5391119268214792",
            cvc = "1AA",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateInstanceWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.CVC])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstanceWithCardWithInvalidExpiry")
    fun shouldReturnErrorWhileGenerateInstanceWithCardWithInvalidExpiry() {
        val params = CardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "DDD",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateInstanceWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalidFormat, result.errors[ParamField.EXPIRY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstanceWithCardWithOutDateExpiry")
    fun shouldReturnErrorWhileGenerateInstanceWithCardWithOutDateExpiry() {
        val params = CardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/15",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateInstanceWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.EXPIRY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstanceWithCardWithMaxOutDateExpiry")
    fun shouldReturnErrorWhileGenerateInstanceWithCardWithMaxOutDateExpiry() {
        val params = CardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/35",
            cardHolder = "Joe Doe",
            pubKey = testPubKey
        )

        val result = sdkCore.generateInstanceWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.EXPIRY])
    }

    @Test
    @Description("shouldReturnErrorWhileGenerateInstanceWithCardWithInvalidPubKey")
    fun shouldReturnErrorWhileGenerateInstanceWithCardWithInvalidPubKey() {
        val params = CardParams(
            pan = "5391119268214792",
            cvc = "123",
            expiryMMYY = "12/25",
            cardHolder = "Joe Doe",
            pubKey = "INVALIDPUBKEY"
        )

        val result = sdkCore.generateInstanceWithCard(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldGenerateInstanceWithBinding")
    fun shouldGenerateInstanceWithBinding() {
        val params = BindingParams(
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "123",
            pubKey = testPubKey
        )

        val result = sdkCore.generateInstanceWithBinding(params)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldGenerateInstanceWithBindingWithoutCVC")
    fun shouldGenerateInstanceWithBindingWithoutCVC() {
        val params = BindingParams(
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = null,
            pubKey = testPubKey
        )

        val result = sdkCore.generateInstanceWithBinding(params)
        assertNotEquals(null, result.token)
        assertEquals(emptyMap<ParamField, String>(), result.errors)
    }

    @Test
    @Description("shouldReturnErrorGenerateInstanceWithBindingWithEmptyBindingID")
    fun shouldReturnErrorGenerateInstanceWithBindingWithEmptyBindingID() {
        val params = BindingParams(
            bindingID = "",
            cvc = "123",
            pubKey = testPubKey
        )

        val result = sdkCore.generateInstanceWithBinding(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.BINDING_ID])
    }

    @Test
    @Description("shouldReturnErrorGenerateInstanceWithBindingWithEmptyPubKey")
    fun shouldReturnErrorGenerateInstanceWithBindingWithEmptyPubKey() {
        val params = BindingParams(
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "123",
            pubKey = ""
        )

        val result = sdkCore.generateInstanceWithBinding(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.required, result.errors[ParamField.PUB_KEY])
    }

    @Test
    @Description("shouldReturnErrorGenerateInstanceWithBindingWithInvalidCVC")
    fun shouldReturnErrorGenerateInstanceWithBindingWithInvalidCVC() {
        val params = BindingParams(
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "aaD",
            pubKey = testPubKey
        )

        val result = sdkCore.generateInstanceWithBinding(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.CVC])
    }

    @Test
    @Description("shouldReturnErrorGenerateInstanceWithBindingWithInvalidPubKey")
    fun shouldReturnErrorGenerateInstanceWithBindingWithInvalidPubKey() {
        val params = BindingParams(
            bindingID = "513b17f4-e32e-414f-8c74-936fd7027baa",
            cvc = "123",
            pubKey = "INVALIDPUBKEY"
        )

        val result = sdkCore.generateInstanceWithBinding(params)
        assertEquals(null, result.token)
        assertEquals(true, result.errors.isNotEmpty())
        assertEquals(ValidationCodes.invalid, result.errors[ParamField.PUB_KEY])
    }
}
