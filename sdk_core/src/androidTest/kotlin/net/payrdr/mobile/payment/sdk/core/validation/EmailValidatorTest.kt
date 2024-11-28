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

@Suppress("LargeClass")
@SmallTest
class EmailValidatorTest {

    @get:Rule
    val permissionRule: TestRule = grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private lateinit var emailValidator: EmailValidator

    @Before
    fun setUp() {
        emailValidator = EmailValidator(targetContext())
    }

    @Test
    @Description("shouldAcceptCorrectEmail1")
    fun shouldAcceptCorrectEmail1() {
        val result = emailValidator.validate("email@example.com")
        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldAcceptCorrectEmail2")
    fun shouldAcceptCorrectEmail2() {
        val result = emailValidator.validate("firstname.lastname@example.com")
        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldAcceptCorrectEmail3")
    fun shouldAcceptCorrectEmail3() {
        val result = emailValidator.validate("email@subdomain.example.com")
        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldAcceptCorrectEmail4")
    fun shouldAcceptCorrectEmail4() {
        val result = emailValidator.validate("firstname+lastname@example.com")
        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldAcceptCorrectEmail5")
    fun shouldAcceptCorrectEmail5() {
        val result = emailValidator.validate("1234567890@example.com")
        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldAcceptCorrectEmail6")
    fun shouldAcceptCorrectEmail6() {
        val result = emailValidator.validate("email@example-one.com")
        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldAcceptCorrectEmail7")
    fun shouldAcceptCorrectEmail7() {
        val result = emailValidator.validate("email@example.name")
        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldAcceptCorrectEmail8")
    fun shouldAcceptCorrectEmail8() {
        val result = emailValidator.validate("email@example.museum")
        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldAcceptCorrectEmail9")
    fun shouldAcceptCorrectEmail9() {
        val result = emailValidator.validate("email@example.co.jp")
        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldAcceptCorrectEmail10")
    fun shouldAcceptCorrectEmail10() {
        val result = emailValidator.validate("firstname-lastname@example.com")
        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptEmptyString")
    fun shouldNotAcceptEmptyString() {
        val result = emailValidator.validate("")
        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_not_empty_required), result.errorMessage)
        assertEquals(ValidationCodes.required, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectEmail1")
    fun shouldNotAcceptIncorrectEmail1() {
        val result = emailValidator.validate("plainaddress")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_incorrect_email), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectEmail2")
    fun shouldNotAcceptIncorrectEmail2() {
        val result = emailValidator.validate("#@%^%#$@#$@#.com")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_incorrect_email), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectEmail3")
    fun shouldNotAcceptIncorrectEmail3() {
        val result = emailValidator.validate("@example.com")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_incorrect_email), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectEmail4")
    fun shouldNotAcceptIncorrectEmail4() {
        val result = emailValidator.validate("Joe Smith <email@example.com>")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_incorrect_email), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectEmail5")
    fun shouldNotAcceptIncorrectEmail5() {
        val result = emailValidator.validate("email.example.com")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_incorrect_email), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectEmail6")
    fun shouldNotAcceptIncorrectEmail6() {
        val result = emailValidator.validate("email@example@example.com")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_incorrect_email), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectEmail7")
    fun shouldNotAcceptIncorrectEmail7() {
        val result = emailValidator.validate(".email@example.com")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_incorrect_email), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectEmail8")
    fun shouldNotAcceptIncorrectEmail8() {
        val result = emailValidator.validate("email.@example.com")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_incorrect_email), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectEmail9")
    fun shouldNotAcceptIncorrectEmail9() {
        val result = emailValidator.validate("email..email@example.com")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_incorrect_email), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectEmail10")
    fun shouldNotAcceptIncorrectEmail10() {
        val result = emailValidator.validate("あいうえお@example.com")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_incorrect_email), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectEmail11")
    fun shouldNotAcceptIncorrectEmail11() {
        val result = emailValidator.validate("email@example.com (Joe Smith)")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_incorrect_email), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectEmail12")
    fun shouldNotAcceptIncorrectEmail12() {
        val result = emailValidator.validate("email@example")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_incorrect_email), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectEmail13")
    fun shouldNotAcceptIncorrectEmail13() {
        val result = emailValidator.validate("email@-example.com")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_incorrect_email), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectEmail14")
    fun shouldNotAcceptIncorrectEmail14() {
        val result = emailValidator.validate("email@111.222.333.44444")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_incorrect_email), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectEmail15")
    fun shouldNotAcceptIncorrectEmail15() {
        val result = emailValidator.validate("email@example..com")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_incorrect_email), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptIncorrectEmail16")
    fun shouldNotAcceptIncorrectEmail16() {
        val result = emailValidator.validate("Abc..123@example.com")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_incorrect_email), result.errorMessage)
        assertEquals(ValidationCodes.invalidFormat, result.errorCode)
    }
}
