package net.payrdr.mobile.payment.sdk.core.validation

import android.content.Context
import net.payrdr.mobile.payment.sdk.core.R
import net.payrdr.mobile.payment.sdk.core.validation.rules.RegExRule
import net.payrdr.mobile.payment.sdk.core.validation.rules.StringRequiredRule

/**
 *  Email value validator.
 *
 *  @param context context for getting string resources.
 * */
class EmailValidator(context: Context): BaseValidator<String>() {

    init {
        addRules(
            StringRequiredRule(
                code = ValidationCodes.required,
                message = context.getString(R.string.payrdr_not_empty_required)
            ),
            RegExRule(
                code = ValidationCodes.invalidFormat,
                message = context.getString(R.string.payrdr_incorrect_email),
                regex = PATTERN
            )
        )
    }

    companion object {
        private val PATTERN = "^[a-zA-z0-9]+([-+.']\\w+)*@[a-z]+([-.][a-z]+)*\\.[a-z]+([-.][a-z]+)*$".toRegex()
    }
}
