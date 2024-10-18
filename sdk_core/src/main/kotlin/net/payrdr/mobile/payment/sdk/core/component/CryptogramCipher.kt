package net.payrdr.mobile.payment.sdk.core.component

import net.payrdr.mobile.payment.sdk.core.model.Key

/**
 * Interface for cryptogram encryptor.
 */
interface CryptogramCipher {

    /**
     * Encrypt [data] by public key [key].
     *
     * @return cryptogram.
     */
    fun encode(data: String, key: Key): String
}
