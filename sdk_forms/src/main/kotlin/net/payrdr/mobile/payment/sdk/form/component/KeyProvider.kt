package net.payrdr.mobile.payment.sdk.form.component

import net.payrdr.mobile.payment.sdk.core.model.Key

/**
 * Key provider interface for encrypting payment information.
 */
interface KeyProvider {

    /**
     * Returns an active key for encrypting payment information.
     *
     * @return active key.
     */
    @Throws(KeyProviderException::class)
    suspend fun provideKey(): Key
}
