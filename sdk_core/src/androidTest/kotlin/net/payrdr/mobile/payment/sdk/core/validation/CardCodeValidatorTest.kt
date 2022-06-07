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
class CardCodeValidatorTest {

    @get:Rule
    val permissionRule: TestRule = grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private lateinit var cardCodeValidator: CardCodeValidator

    @Before
    fun setUp() {
        cardCodeValidator =
            CardCodeValidator(targetContext())
    }

    @Test
    @Description("shouldAcceptCorrectCode")
    fun shouldAcceptCorrectCode() {
        val result = cardCodeValidator.validate("123")

        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptEmptyString")
    fun shouldNotAcceptEmptyString() {
        val result = cardCodeValidator.validate("")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_card_incorrect_cvc), result.errorMessage)
        assertEquals(ValidationCodes.required, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptLessThanMinLength")
    fun shouldNotAcceptLessThanMinLength() {
        val result = cardCodeValidator.validate("12")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_card_incorrect_cvc), result.errorMessage)
        assertEquals(ValidationCodes.invalid, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptMoreThanMaxLength")
    fun shouldNotAcceptMoreThanMaxLength() {
        val result = cardCodeValidator.validate("1234")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_card_incorrect_cvc), result.errorMessage)
        assertEquals(ValidationCodes.invalid, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptWithLetterSymbols")
    fun shouldNotAcceptWithLetterSymbols() {
        val result = cardCodeValidator.validate("1AAA")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_card_incorrect_cvc), result.errorMessage)
        assertEquals(ValidationCodes.invalid, result.errorCode)
    }
}
