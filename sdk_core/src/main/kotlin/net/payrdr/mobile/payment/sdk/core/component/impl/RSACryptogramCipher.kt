package net.payrdr.mobile.payment.sdk.core.component.impl

import android.util.Base64
import net.payrdr.mobile.payment.sdk.core.component.CryptogramCipher
import net.payrdr.mobile.payment.sdk.core.model.Key
import net.payrdr.mobile.payment.sdk.core.utils.pemKeyContent
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

/**
 * Implementation of cryptogram encryptor.
 */
class RSACryptogramCipher : CryptogramCipher {

    override fun encode(data: String, key: Key): String {
        check(key.protocol == "RSA")
        val keyPem = key.value.pemKeyContent()
        val keyBytes = Base64.decode(keyPem.toByteArray(), Base64.DEFAULT)
        val spec = X509EncodedKeySpec(keyBytes)
        val fact = KeyFactory.getInstance("RSA")

        val cipher = Cipher.getInstance("RSA/NONE/PKCS1PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, fact.generatePublic(spec))
        val encoded = cipher.doFinal(data.toByteArray())

        return Base64.encodeToString(encoded, Base64.NO_WRAP)
    }
}
