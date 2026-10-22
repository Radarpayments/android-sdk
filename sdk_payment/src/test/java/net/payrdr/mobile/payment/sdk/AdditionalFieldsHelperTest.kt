package net.payrdr.mobile.payment.sdk

import android.text.Editable
import android.view.View
import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.payrdr.mobile.payment.sdk.model.PaymentSystem
import net.payrdr.mobile.payment.sdk.ui.widget.BaseTextInputEditText
import net.payrdr.mobile.payment.sdk.ui.widget.BaseTextInputLayout
import org.junit.Assert.assertThrows
import org.junit.Test

@SmallTest
class AdditionalFieldsHelperTest {

    // ---- resolvePaymentSystem ----

    @Test
    fun shouldResolveVisaByPrefix4() {
        AdditionalFieldsHelper.resolvePaymentSystem("4111111111111111") shouldBe PaymentSystem.VISA
    }

    @Test
    fun shouldResolveMastercardByClassicPrefixes() {
        listOf("5100000000000000", "5555555555554444", "5300000000000000")
            .forEach {
                AdditionalFieldsHelper.resolvePaymentSystem(it) shouldBe PaymentSystem.MASTERCARD
            }
    }

    @Test
    fun shouldResolveMastercardByModernRange() {
        // 2221-2720 range introduced for Mastercard.
        AdditionalFieldsHelper.resolvePaymentSystem("2221000000000000") shouldBe PaymentSystem.MASTERCARD
        AdditionalFieldsHelper.resolvePaymentSystem("2500000000000000") shouldBe PaymentSystem.MASTERCARD
        AdditionalFieldsHelper.resolvePaymentSystem("2720000000000000") shouldBe PaymentSystem.MASTERCARD
    }

    @Test
    fun shouldReturnOtherSystemForAmexJcbMaestro() {
        AdditionalFieldsHelper.resolvePaymentSystem("378282246310005") shouldBe PaymentSystem.OTHER_SYSTEM
        AdditionalFieldsHelper.resolvePaymentSystem("3530111333300000") shouldBe PaymentSystem.OTHER_SYSTEM
        AdditionalFieldsHelper.resolvePaymentSystem("6304000000000000") shouldBe PaymentSystem.OTHER_SYSTEM
    }

    @Test
    fun shouldStripNonDigitsBeforeResolving() {
        AdditionalFieldsHelper.resolvePaymentSystem("4111-1111-1111-1111") shouldBe PaymentSystem.VISA
        AdditionalFieldsHelper.resolvePaymentSystem("5555 5555 5555 4444") shouldBe PaymentSystem.MASTERCARD
    }

    @Test
    fun shouldReturnOtherSystemForEmptyOrUnknownPan() {
        AdditionalFieldsHelper.resolvePaymentSystem("") shouldBe PaymentSystem.OTHER_SYSTEM
        AdditionalFieldsHelper.resolvePaymentSystem("9999999999999999") shouldBe PaymentSystem.OTHER_SYSTEM
    }

    // ---- resolveFieldIdByName ----

    @Test
    fun shouldResolveAllSupportedFieldNames() {
        val expected = mapOf(
            "EMAIL" to (R.id.emailInputLayout to R.id.emailInput),
            "MOBILE_PHONE" to (R.id.phoneNumberInputLayout to R.id.phoneNumberInput),
            "BILLING_ADDRESS_LINE1" to (R.id.addressLine1InputLayout to R.id.addressLine1Input),
            "BILLING_ADDRESS_LINE2" to (R.id.addressLine2InputLayout to R.id.addressLine2Input),
            "BILLING_ADDRESS_LINE3" to (R.id.addressLine3InputLayout to R.id.addressLine3Input),
            "BILLING_COUNTRY" to (R.id.countryInputLayout to R.id.countryInput),
            "BILLING_CITY" to (R.id.cityInputLayout to R.id.cityInput),
            "BILLING_STATE" to (R.id.stateInputLayout to R.id.stateInput),
            "BILLING_POSTAL_CODE" to (R.id.postalCodeInputLayout to R.id.postalCodeInput),
        )

        expected.forEach { (name, ids) ->
            AdditionalFieldsHelper.resolveFieldIdByName(name) shouldBe ids
        }
    }

    @Test
    fun shouldThrowForUnknownFieldName() {
        assertThrows(IllegalStateException::class.java) {
            AdditionalFieldsHelper.resolveFieldIdByName("NOT_A_REAL_FIELD")
        }
    }

    // ---- prepareAdditionalParam ----

    @Test
    fun shouldReturnNullWhenLayoutNotVisible() {
        val layout = mockk<BaseTextInputLayout>()
        val input = mockk<BaseTextInputEditText>()
        every { layout.visibility } returns View.GONE

        AdditionalFieldsHelper.prepareAdditionalParam(layout, input) shouldBe null
    }

    @Test
    fun shouldReturnNullWhenTextIsNull() {
        val layout = mockk<BaseTextInputLayout>()
        val input = mockk<BaseTextInputEditText>()
        every { layout.visibility } returns View.VISIBLE
        every { input.text } returns null

        AdditionalFieldsHelper.prepareAdditionalParam(layout, input) shouldBe null
    }

    @Test
    fun shouldReturnNullWhenTextIsBlank() {
        val layout = mockk<BaseTextInputLayout>()
        val input = mockk<BaseTextInputEditText>()
        val editable = mockk<Editable>()
        val backing = "   "
        every { layout.visibility } returns View.VISIBLE
        every { input.text } returns editable
        every { editable.length } returns backing.length
        every { editable[any()] } answers { backing[firstArg<Int>()] }
        every { editable.subSequence(any(), any()) } answers {
            backing.subSequence(firstArg<Int>(), secondArg<Int>())
        }

        AdditionalFieldsHelper.prepareAdditionalParam(layout, input) shouldBe null
    }

    @Test
    fun shouldReturnTextWhenVisibleAndNonBlank() {
        val layout = mockk<BaseTextInputLayout>()
        val input = mockk<BaseTextInputEditText>()
        val editable = mockk<Editable>()
        val backing = "Moscow"
        every { layout.visibility } returns View.VISIBLE
        every { input.text } returns editable
        every { editable.length } returns backing.length
        every { editable[any()] } answers { backing[firstArg<Int>()] }
        every { editable.subSequence(any(), any()) } answers {
            backing.subSequence(firstArg<Int>(), secondArg<Int>())
        }
        every { editable.toString() } returns backing

        AdditionalFieldsHelper.prepareAdditionalParam(layout, input) shouldBe "Moscow"
    }
}
