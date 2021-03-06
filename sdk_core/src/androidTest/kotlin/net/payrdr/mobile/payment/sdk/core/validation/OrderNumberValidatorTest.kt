package net.payrdr.mobile.payment.sdk.core.validation

import android.Manifest
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule.grant
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.core.R
import net.payrdr.mobile.payment.sdk.core.test.getString
import net.payrdr.mobile.payment.sdk.core.test.targetContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@SmallTest
class OrderNumberValidatorTest {

    @get:Rule
    val permissionRule: TestRule = grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private lateinit var orderNumberValidator: OrderNumberValidator

    @Before
    fun setUp() {
        orderNumberValidator = OrderNumberValidator(targetContext())
    }

    @Test
    @Description("shouldAcceptCorrectOrderNumber")
    fun shouldAcceptCorrectOrderNumber() {
        val result = orderNumberValidator.validate("39ce26e1-5fd0-4784-9e6c-25c9f2c2d09e")

        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptEmptyOrderNumber")
    fun shouldNotAcceptEmptyOrderNumber() {
        val result = orderNumberValidator.validate("")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_order_incorrect_length), result.errorMessage)
        assertEquals(ValidationCodes.required, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptBlankOrderNumber")
    fun shouldNotAcceptBlankOrderNumber() {
        val result = orderNumberValidator.validate("    ")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_order_incorrect_length), result.errorMessage)
        assertEquals(ValidationCodes.required, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptWithSpaceOrderNumber")
    fun shouldNotAcceptWithSpaceOrderNumber() {
        val result = orderNumberValidator.validate("  39ce26e1 -5fd 0-4784-9e6c-25c9f2c2d09e")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_order_incorrect_length), result.errorMessage)
        assertEquals(ValidationCodes.invalid, result.errorCode)
    }
}
