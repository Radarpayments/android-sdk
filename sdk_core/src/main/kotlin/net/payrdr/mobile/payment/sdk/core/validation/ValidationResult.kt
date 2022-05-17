package net.payrdr.mobile.payment.sdk.core.validation

/**
 * Description of the data validation result.
 *
 * @param isValid true the data is correct, otherwise false.
 * @param errorCode error code, does not change during localization.
 * @param errorMessage error message in the data, if it was detected during validation.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorCode: String?,
    val errorMessage: String?
) {
    companion object {
        /**
         * @return validation result.
         */
        val VALID = ValidationResult(true, null, null)

        /**
         * Method describing the error .
         *
         * @param errorCode error code.
         * @param errorMessage message displayed in case of incorrect value of the card parameter.
         *
         * @return validation result.
         * */
        fun error(errorCode: String, errorMessage: String) =
            ValidationResult(false, errorCode, errorMessage)
    }
}
