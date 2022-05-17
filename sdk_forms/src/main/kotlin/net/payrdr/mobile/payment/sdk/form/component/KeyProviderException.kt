package net.payrdr.mobile.payment.sdk.form.component

/**
 * An error that occurs when obtaining an encryption key.
 *
 * @param message error description text.
 * @param cause error reason.
 */
class KeyProviderException(override val message: String, override val cause: Throwable?) :
    RuntimeException(message, cause)
