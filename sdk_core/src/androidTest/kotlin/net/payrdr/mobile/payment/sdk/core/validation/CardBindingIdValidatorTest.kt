package net.payrdr.mobile.payment.sdk.core.validation

import android.Manifest
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule.grant
import io.qameta.allure.android.runners.AllureAndroidJUnit4
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
import org.junit.runner.RunWith

@SmallTest
@RunWith(AllureAndroidJUnit4::class)
class CardBindingIdValidatorTest {

    @get:Rule
    val permissionRule: TestRule = grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private lateinit var cardBindingIdValidator: CardBindingIdValidator

    @Before
    fun setUp() {
        cardBindingIdValidator = CardBindingIdValidator(targetContext())
    }

    @Test
    @Description("shouldReturnProcessFormObject")
    fun shouldAcceptCorrectBindingId() {
        val result = cardBindingIdValidator.validate("513b17f4-e32e-414f-8c74-936fd7027baa")

        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldReturnProcessFormObject")
    fun shouldNotAcceptEmptyBindingId() {
        val result = cardBindingIdValidator.validate("")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_binding_required), result.errorMessage)
        assertEquals(ValidationCodes.required, result.errorCode)
    }

    @Test
    @Description("shouldReturnProcessFormObject")
    fun shouldNotAcceptBlankBindingId() {
        val result = cardBindingIdValidator.validate("   ")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_binding_required), result.errorMessage)
        assertEquals(ValidationCodes.required, result.errorCode)
    }

    @Test
    @Description("shouldReturnProcessFormObject")
    fun shouldNotAcceptWithSpaceBindingId() {
        val result = cardBindingIdValidator.validate("513b17f4 - e32e-414f-8c74-936fd7027baa")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_binding_incorrect), result.errorMessage)
        assertEquals(ValidationCodes.invalid, result.errorCode)
    }
}
