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
class CardHolderValidatorTest {

    @get:Rule
    val permissionRule: TestRule = grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private lateinit var cardHolderValidator: CardHolderValidator

    @Before
    fun setUp() {
        cardHolderValidator =
            CardHolderValidator(targetContext())
    }

    @Test
    @Description("shouldAcceptCorrectName")
    fun shouldAcceptCorrectName() {
        val resultFirst = cardHolderValidator.validate("John Doe")
        assertTrue(resultFirst.isValid)
        assertNull(resultFirst.errorMessage)
        assertNull(resultFirst.errorCode)

        val secondResult = cardHolderValidator.validate("Diana Anika")
        assertTrue(secondResult.isValid)
        assertNull(secondResult.errorMessage)
        assertNull(secondResult.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectName")
    fun shouldNotAcceptIncorrectName() {
        val result = cardHolderValidator.validate("Guðmundur Halima")
        assertFalse(result.isValid)
        assertEquals(getString(R.string.payrdr_card_incorrect_card_holder), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptDigits")
    fun shouldNotAcceptDigits() {
        val result = cardHolderValidator.validate("665361 165654")
        assertFalse(result.isValid)
        assertEquals(getString(R.string.payrdr_card_incorrect_card_holder), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptMoreThanMaxLength")
    fun shouldNotAcceptMoreThanMaxLength() {
        val result = cardHolderValidator.validate("John DoeEEEEEEEEEEEEEEEEEEEEEEE")
        assertFalse(result.isValid)
        assertEquals(getString(R.string.payrdr_card_incorrect_card_holder), result.errorMessage)
        assertEquals(ValidationCodes.invalid, result.errorCode)
    }
}
