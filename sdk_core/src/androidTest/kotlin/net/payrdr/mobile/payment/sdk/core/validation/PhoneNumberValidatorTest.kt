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
class PhoneNumberValidatorTest {

    @get:Rule
    val permissionRule: TestRule = grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private lateinit var phoneNumberValidator: PhoneNumberValidator

    @Before
    fun setUp() {
        phoneNumberValidator = PhoneNumberValidator(targetContext())
    }

    @Test
    @Description("shouldAcceptCorrectPhone")
    fun shouldAcceptCorrectPhone() {
        val result = phoneNumberValidator.validate("35799902871")

        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldAcceptCorrectPhoneWithoutPlus")
    fun shouldAcceptCorrectPhoneWithoutPlus() {
        val result = phoneNumberValidator.validate("83539233825")

        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptEmptyString")
    fun shouldNotAcceptEmptyString() {
        val result = phoneNumberValidator.validate("")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_not_empty_required), result.errorMessage)
        assertEquals(ValidationCodes.required, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptPhoneWithFewPlusSigns")
    fun shouldNotAcceptPhoneWithFewPlusSigns() {
        val result = phoneNumberValidator.validate("++83539233825")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_incorrect_phone_number), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }
}
