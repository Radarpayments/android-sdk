package net.payrdr.mobile.payment.sdk.core.validation.rules

import net.payrdr.mobile.payment.sdk.core.validation.BaseValidationRule
import net.payrdr.mobile.payment.sdk.core.validation.ValidationResult
import net.payrdr.mobile.payment.sdk.core.validation.ValidationResult.Companion.VALID
import net.payrdr.mobile.payment.sdk.core.validation.ValidationResult.Companion.error

/**
 * Rule for checking a string value against a regular expression.
 *
 * @param code error code.
 * @param message message displayed in case of mismatch of regular expression .
 * @param regex regex to test a string .
 */
class RegExRule(
    private val code: String,
    private val message: String,
    private val regex: Regex
) : BaseValidationRule<String> {
    override fun validateForError(data: String): ValidationResult = if (!data.matches(regex)) {
        error(code, message)
    } else {
        VALID
    }
}
