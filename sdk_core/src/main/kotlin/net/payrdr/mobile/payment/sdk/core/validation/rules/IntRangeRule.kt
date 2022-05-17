package net.payrdr.mobile.payment.sdk.core.validation.rules

import net.payrdr.mobile.payment.sdk.core.validation.BaseValidationRule
import net.payrdr.mobile.payment.sdk.core.validation.ValidationResult
import net.payrdr.mobile.payment.sdk.core.validation.ValidationResult.Companion.VALID

/**
 * Rule to check for a numeric value in a specified range.
 *
 * @param code error code.
 * @param message message displayed when a numeric value is out of range.
 * @param min minimum allowable value.
 * @param max maximum allowable value .
 */
class IntRangeRule(
    private val code: String,
    private val message: String,
    private val min: Int = 0,
    private val max: Int = Int.MAX_VALUE
) : BaseValidationRule<Int> {
    override fun validateForError(data: Int): ValidationResult {
        return if (data < min || data > max) {
            ValidationResult.error(code, message)
        } else {
            VALID
        }
    }
}
