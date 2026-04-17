package net.payrdr.mobile.payment.sdk.core.validation

import android.Manifest
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule.grant
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.core.R
import net.payrdr.mobile.payment.sdk.core.test.getString
import net.payrdr.mobile.payment.sdk.core.test.targetContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@SmallTest
class PayerAdditionalInformationValidatorTest {

    @get:Rule
    val permissionRule: TestRule = grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private lateinit var validator: PayerAdditionalInformationValidator

    @Before
    fun setUp() {
        validator = PayerAdditionalInformationValidator(targetContext())
    }

    @Test
    @Description("shouldAcceptNonEmptyValue")
    fun shouldAcceptNonEmptyValue() {
        val result = validator.validate("some value")

        assertTrue(result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldRejectEmptyValue")
    fun shouldRejectEmptyValue() {
        val result = validator.validate("")

        assertFalse(result.isValid)
        assertEquals(getString(R.string.payrdr_not_empty_required), result.errorMessage)
        assertEquals(ValidationCodes.required, result.errorCode)
    }

    @Test
    @Description("shouldRejectBlankValue")
    fun shouldRejectBlankValue() {
        val result = validator.validate("   ")

        assertFalse(result.isValid)
        assertEquals(getString(R.string.payrdr_not_empty_required), result.errorMessage)
        assertEquals(ValidationCodes.required, result.errorCode)
    }
}
