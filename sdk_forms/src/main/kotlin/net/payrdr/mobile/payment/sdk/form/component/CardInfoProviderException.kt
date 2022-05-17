package net.payrdr.mobile.payment.sdk.form.component

/**
 * An error that occurs when obtaining information about the card.
 *
 * @param message error description text.
 * @param cause error reason.
 */
class CardInfoProviderException(override val message: String, override val cause: Throwable?) :
    RuntimeException(message, cause)
