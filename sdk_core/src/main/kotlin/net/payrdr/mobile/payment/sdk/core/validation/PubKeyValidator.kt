package net.payrdr.mobile.payment.sdk.core.validation

import android.content.Context
import net.payrdr.mobile.payment.sdk.core.R
import net.payrdr.mobile.payment.sdk.core.validation.rules.StringRequiredRule

/**
 *  Validator to check the validity of the public key.
 *
 *  @param context application context.
 * */
class PubKeyValidator(context: Context) : BaseValidator<String>() {

    init {
        addRules(
            StringRequiredRule(
                code = ValidationCodes.required,
                message = context.getString(R.string.payrdr_pub_key_required)
            )
        )
    }
}
