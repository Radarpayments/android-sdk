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
class CardNumberValidatorTest {

    @get:Rule
    val permissionRule: TestRule = grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private lateinit var cardNumberValidator: CardNumberValidator

    @Before
    fun setUp() {
        cardNumberValidator =
            CardNumberValidator(targetContext())
    }

    @Test
    @Description("shouldAcceptCorrectNumber")
    fun shouldAcceptCorrectNumber() {
        val resultFirst = cardNumberValidator.validate("4556733604106746")
        assertTrue(resultFirst.isValid)
        assertNull(resultFirst.errorMessage)
        assertNull(resultFirst.errorCode)

        val resultSecond = cardNumberValidator.validate("4539985984741055997")
        assertTrue(resultSecond.isValid)
        assertNull(resultSecond.errorMessage)
        assertNull(resultSecond.errorCode)
    }

    @Test
    @Description("shouldNotAcceptLessThanMinLength")
    fun shouldNotAcceptLessThanMinLength() {
        cardNumberValidator.validate("455673360410674").let {
            assertFalse(it.isValid)
            assertEquals(getString(R.string.payrdr_card_incorrect_length), it.errorMessage)
            assertEquals(ValidationCodes.invalid, it.errorCode)
        }
    }

    @Test
    @Description("shouldNotAcceptEmptyLine")
    fun shouldNotAcceptEmptyLine() {
        cardNumberValidator.validate("").let {
            assertFalse(it.isValid)
            assertEquals(getString(R.string.payrdr_card_incorrect_number), it.errorMessage)
            assertEquals(ValidationCodes.required, it.errorCode)
        }
    }

    @Test
    @Description("shouldNotAcceptMoreThanMaxLength")
    fun shouldNotAcceptMoreThanMaxLength() {
        cardNumberValidator.validate("45399859847410559971").let {
            assertFalse(it.isValid)
            assertEquals(getString(R.string.payrdr_card_incorrect_length), it.errorMessage)
            assertEquals(ValidationCodes.invalid, it.errorCode)
        }
    }

    @Test
    @Description("shouldNotAcceptNotDigits")
    fun shouldNotAcceptNotDigits() {
        cardNumberValidator.validate("IncorrectCardNum").let {
            assertFalse(it.isValid)
            assertEquals(getString(R.string.payrdr_card_incorrect_number), it.errorMessage)
            assertEquals(ValidationCodes.invalidFormat, it.errorCode)
        }
    }

    @Test
    @Description("shouldNotAcceptIfLunhFailed")
    fun shouldNotAcceptIfLunhFailed() {
        cardNumberValidator.validate("4532047793306966344").let {
            assertFalse(it.isValid)
            assertEquals(getString(R.string.payrdr_card_incorrect_number), it.errorMessage)
            assertEquals(ValidationCodes.invalid, it.errorCode)
        }

        cardNumberValidator.validate("4556733604106745").let {
            assertFalse(it.isValid)
            assertEquals(getString(R.string.payrdr_card_incorrect_number), it.errorMessage)
            assertEquals(ValidationCodes.invalid, it.errorCode)
        }
    }
}
