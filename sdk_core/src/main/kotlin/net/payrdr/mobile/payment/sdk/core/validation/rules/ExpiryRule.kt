package net.payrdr.mobile.payment.sdk.core.validation.rules

import net.payrdr.mobile.payment.sdk.core.utils.digitsOnly
import net.payrdr.mobile.payment.sdk.core.validation.BaseValidationRule
import net.payrdr.mobile.payment.sdk.core.validation.ValidationResult
import net.payrdr.mobile.payment.sdk.core.validation.ValidationResult.Companion.VALID
import java.util.Calendar

/**
 * Rule for checking the right of the card expiration format.
 *
 * @param code error code.
 * @param message message displayed in case of incorrect value of the card validity period.
 */
class ExpiryRule(
    private val code: String,
    private val message: String
) : BaseValidationRule<String> {
    private val monthChecker =
        IntRangeRule(
            code = code,
            message = message,
            min = MONTH_MIN,
            max = MONTH_MAX
        )

    private val yearChecker =
        IntRangeRule(
            code = code,
            message = message,
            min = YEAR_MIN,
            max = YEAR_MAX
        )

    override fun validateForError(data: String): ValidationResult {
        val digits = data.digitsOnly()
        val month = digits.take(2).toIntOrNull() ?: INVALID_FIELD_VALUE
        val year = digits.takeLast(2).toIntOrNull() ?: INVALID_FIELD_VALUE
        val monthValidationResult = monthChecker.validateForError(month)
        val yearValidationResult = yearChecker.validateForError(year)
        return when {
            !monthValidationResult.isValid -> monthValidationResult
            !yearValidationResult.isValid -> yearValidationResult
            else -> VALID
        }
    }

    companion object {
        private const val MONTH_MIN = 1
        private const val MONTH_MAX = 12
        private const val INVALID_FIELD_VALUE = -1
        private const val MAX_YEARS = 10
        private val YEAR_MIN = Calendar.getInstance().get(Calendar.YEAR) % 100
        private val YEAR_MAX = (Calendar.getInstance().get(Calendar.YEAR) + MAX_YEARS) % 100
    }
}
