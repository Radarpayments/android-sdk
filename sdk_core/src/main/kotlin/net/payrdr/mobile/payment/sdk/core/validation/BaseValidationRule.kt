package net.payrdr.mobile.payment.sdk.core.validation

/**
 * Base class for creating data validation rules .
 */
interface BaseValidationRule<DATA> {

    /**
     * The method is called when validating data [data].
     *
     * @param data data for checking.
     * @return null if the data matches the rule, otherwise the text is paired with the value of
     * the error code and the error text.
     */
    fun validateForError(data: DATA): ValidationResult
}
