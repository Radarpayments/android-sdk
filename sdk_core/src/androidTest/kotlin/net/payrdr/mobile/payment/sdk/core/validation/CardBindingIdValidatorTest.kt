package net.payrdr.mobile.payment.sdk.core.validation

import android.Manifest
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule.grant
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
class CardBindingIdValidatorTest {

    @get:Rule
    val permissionRule: TestRule = grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private lateinit var cardBindingIdValidator: CardBindingIdValidator

    @Before
    fun setUp() {
        cardBindingIdValidator = CardBindingIdValidator(targetContext())
    }

    @Test
    fun shouldAcceptCorrectBindingId() {
        val result = cardBindingIdValidator.validate("513b17f4-e32e-414f-8c74-936fd7027baa")

        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    fun shouldNotAcceptEmptyBindingId() {
        val result = cardBindingIdValidator.validate("")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_binding_required), result.errorMessage)
        assertEquals(ValidationCodes.required, result.errorCode)
    }

    @Test
    fun shouldNotAcceptBlankBindingId() {
        val result = cardBindingIdValidator.validate("   ")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_binding_required), result.errorMessage)
        assertEquals(ValidationCodes.required, result.errorCode)
    }

    @Test
    fun shouldNotAcceptWithSpaceBindingId() {
        val result = cardBindingIdValidator.validate("513b17f4 - e32e-414f-8c74-936fd7027baa")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_binding_incorrect), result.errorMessage)
        assertEquals(ValidationCodes.invalid, result.errorCode)
    }
}
