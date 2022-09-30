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
class CardExpiryValidatorTest {

    @get:Rule
    val permissionRule: TestRule = grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private lateinit var cardExpiryValidator: CardExpiryValidator

    @Before
    fun setUp() {
        cardExpiryValidator =
            CardExpiryValidator(targetContext())
    }

    @Test
    @Description("shouldAcceptCorrectCode")
    fun shouldAcceptCorrectCode() {
        val result = cardExpiryValidator.validate("12/29")

        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldAcceptExpiredDate")
    fun shouldAcceptExpiredDate() {
        with(cardExpiryValidator.validate("12/20")) {
            assertEquals(true, isValid)
            assertNull(errorMessage)
            assertNull(errorCode)
        }

        with(cardExpiryValidator.validate("12/01")) {
            assertEquals(true, isValid)
            assertNull(errorMessage)
            assertNull(errorCode)
        }
    }

    @Test
    @Description("shouldAcceptMaxExpiryDate")
    fun shouldAcceptMaxExpiryDate() {
        with(cardExpiryValidator.validate("12/99")) {
            assertEquals(true, isValid)
            assertNull(errorMessage)
            assertNull(errorCode)
        }
    }

    @Test
    @Description("shouldNotAcceptLessThanMinLength")
    fun shouldNotAcceptLessThanMinLength() {
        val result = cardExpiryValidator.validate("12")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_card_incorrect_expiry), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptMoreThanMaxLength")
    fun shouldNotAcceptMoreThanMaxLength() {
        val result = cardExpiryValidator.validate("12/346")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_card_incorrect_expiry), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptMoreIncorrectFormat")
    fun shouldNotAcceptMoreIncorrectFormat() {
        val result = cardExpiryValidator.validate("12346")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_card_incorrect_expiry), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptEmptyValue")
    fun shouldNotAcceptEmptyValue() {
        val result = cardExpiryValidator.validate("")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_card_incorrect_expiry), result.errorMessage)
        assertEquals(ValidationCodes.required, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectMonth")
    fun shouldNotAcceptIncorrectMonth() {
        val result = cardExpiryValidator.validate("13/25")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_card_incorrect_expiry), result.errorMessage)
        assertEquals(ValidationCodes.invalid, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectLastYear")
    fun shouldNotAcceptIncorrectLastYear() {
        val result = cardExpiryValidator.validate("13/19")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_card_incorrect_expiry), result.errorMessage)
        assertEquals(ValidationCodes.invalid, result.errorCode)
    }
}
