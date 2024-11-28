package net.payrdr.mobile.payment.sdk.core.validation

import android.content.Context
import net.payrdr.mobile.payment.sdk.core.R
import net.payrdr.mobile.payment.sdk.core.validation.rules.StringRequiredRule

/**
 * Payer additional information validator.
 * @param context context for getting string resources.
 */
class PayerAdditionalInformationValidator(context: Context) : BaseValidator<String>() {
    init {
        addRules(
            StringRequiredRule(
                code = ValidationCodes.required,
                message = context.getString(R.string.payrdr_not_empty_required)
            )
        )
    }
}
