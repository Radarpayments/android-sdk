package net.payrdr.mobile.payment.sdk.core.validation

/**
 * Base class for creating data validators.
 */
open class BaseValidator<DATA> {

    private val rules = mutableListOf<BaseValidationRule<DATA>>()

    /**
     * Adding validation rules to the validator.
     */
    fun addRules(vararg checker: BaseValidationRule<DATA>) {
        rules.addAll(checker)
    }

    /**
     * Data validation [data] against a list of predefined validation rules.
     *
     * @param data data for checking.
     */
    fun validate(data: DATA): ValidationResult {
        rules.forEach { checker ->
            val result = checker.validateForError(data)
            if (!result.isValid) {
                return result
            }
        }
        return ValidationResult(true, null, null)
    }
}
