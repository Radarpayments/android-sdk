package net.payrdr.mobile.payment.sdk.form

import android.view.View.VISIBLE
import net.payrdr.mobile.payment.sdk.core.utils.digitsOnly
import net.payrdr.mobile.payment.sdk.form.model.AdditionalField
import net.payrdr.mobile.payment.sdk.form.model.PaymentSystem
import net.payrdr.mobile.payment.sdk.form.ui.widget.BaseTextInputEditText
import net.payrdr.mobile.payment.sdk.form.ui.widget.BaseTextInputLayout
import net.payrdr.mobile.payment.sdk.form.ui.widget.PayerAdditionalInformationEditText
import net.payrdr.mobile.payment.sdk.form.utils.onDisplayError

/**
 * Class for configuration additional fields about payer.
 */
object AdditionalFieldsHelper {

    fun configureField(
        additionalField: AdditionalField,
        fieldInputLayout: BaseTextInputLayout,
        fieldInput: BaseTextInputEditText
    ) {
        showField(fieldInputLayout, fieldInput, additionalField.prefilledValue)
        setUpValidator(additionalField.isMandatory, fieldInputLayout, fieldInput)
    }

    private fun showField(
        inputLayout: BaseTextInputLayout,
        fieldInput: BaseTextInputEditText,
        value: String?
    ) {
        inputLayout.visibility = VISIBLE
        fieldInput.setText(value)
    }

    private fun setUpValidator(
        isMandatory: Boolean,
        fieldInputLayout: BaseTextInputLayout,
        fieldInput: BaseTextInputEditText
    ) {
        if (isMandatory && fieldInput is PayerAdditionalInformationEditText) {
            fieldInput.setValidator()
        }
        fieldInput onDisplayError { fieldInputLayout.error = it }
    }

    fun resolveFieldIdByName(name: String): Pair<Int, Int> {
        return when (name) {
            "EMAIL" -> Pair(R.id.emailInputLayout, R.id.emailInput)
            "MOBILE_PHONE" -> Pair(R.id.phoneNumberInputLayout, R.id.phoneNumberInput)
            "BILLING_ADDRESS_LINE1" -> Pair(R.id.addressLine1InputLayout, R.id.addressLine1Input)
            "BILLING_ADDRESS_LINE2" -> Pair(R.id.addressLine2InputLayout, R.id.addressLine2Input)
            "BILLING_ADDRESS_LINE3" -> Pair(R.id.addressLine3InputLayout, R.id.addressLine3Input)
            "BILLING_COUNTRY" -> Pair(R.id.countryInputLayout, R.id.countryInput)
            "BILLING_CITY" -> Pair(R.id.cityInputLayout, R.id.cityInput)
            "BILLING_STATE" -> Pair(R.id.stateInputLayout, R.id.stateInput)
            "BILLING_POSTAL_CODE" -> Pair(R.id.postalCodeInputLayout, R.id.postalCodeInput)
            else -> throw IllegalStateException()
        }
    }

    fun resolvePaymentSystem(pan: String): PaymentSystem {
        val cleanPan = pan.digitsOnly()
        val paymentSystem = PAYMENT_SYSTEMS.filter { cleanPan.matches(it.key) }.toList().firstOrNull()?.second
        return when (paymentSystem) {
            "mastercard" -> PaymentSystem.MASTERCARD
            "visa" -> PaymentSystem.VISA
            else -> PaymentSystem.OTHER_SYSTEM
        }
    }

    fun prepareAdditionalParam(
        inputLayout: BaseTextInputLayout,
        input: BaseTextInputEditText
    ): String? {
        if (inputLayout.visibility != VISIBLE || input.text?.trim().isNullOrEmpty()) return null
        return input.text.toString()
    }

    private val PAYMENT_SYSTEMS = mapOf(
        "^(5[1-5]|222[1-9]|2[3-6]|27[0-1]|2720)\\d*\$".toRegex() to "mastercard",
        "^4\\d*\$".toRegex() to "visa",
    )
}
