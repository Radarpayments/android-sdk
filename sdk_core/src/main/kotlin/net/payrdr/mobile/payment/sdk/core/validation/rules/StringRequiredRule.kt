package net.payrdr.mobile.payment.sdk.core.validation.rules

import net.payrdr.mobile.payment.sdk.core.validation.BaseValidationRule
import net.payrdr.mobile.payment.sdk.core.validation.ValidationResult

/**
 * Rule for checking a field for empty value.
 *
 * @param code error code.
 * @param message message displayed when a numeric value is out of range.
 */

class StringRequiredRule(
    private val code: String,
    private val message: String
) : BaseValidationRule<String> {

    override fun validateForError(data: String): ValidationResult {
        return if (data.isBlank()) {
            ValidationResult.error(code, message)
        } else {
            ValidationResult.VALID
        }
    }
}
