package net.payrdr.mobile.payment.sdk.core

import android.content.Context
import android.util.Log
import net.payrdr.mobile.payment.sdk.core.component.CryptogramCipher
import net.payrdr.mobile.payment.sdk.core.component.PaymentStringProcessor
import net.payrdr.mobile.payment.sdk.core.component.impl.DefaultPaymentStringProcessor
import net.payrdr.mobile.payment.sdk.core.component.impl.RSACryptogramCipher
import net.payrdr.mobile.payment.sdk.core.model.BindingParams
import net.payrdr.mobile.payment.sdk.core.model.CardBindingIdIdentifier
import net.payrdr.mobile.payment.sdk.core.model.CardInfo
import net.payrdr.mobile.payment.sdk.core.model.CardPanIdentifier
import net.payrdr.mobile.payment.sdk.core.model.CardParams
import net.payrdr.mobile.payment.sdk.core.model.Key
import net.payrdr.mobile.payment.sdk.core.model.ParamField
import net.payrdr.mobile.payment.sdk.core.utils.toExpDate
import net.payrdr.mobile.payment.sdk.core.validation.CardBindingIdValidator
import net.payrdr.mobile.payment.sdk.core.validation.CardCodeValidator
import net.payrdr.mobile.payment.sdk.core.validation.CardExpiryValidator
import net.payrdr.mobile.payment.sdk.core.validation.CardHolderValidator
import net.payrdr.mobile.payment.sdk.core.validation.CardNumberValidator
import net.payrdr.mobile.payment.sdk.core.validation.OrderNumberValidator
import net.payrdr.mobile.payment.sdk.core.validation.PubKeyValidator
import java.util.UUID

/**
 * @param context context for getting string resources.
 */
class SDKCore(context: Context) {

    private val paymentStringProcessor: PaymentStringProcessor = DefaultPaymentStringProcessor()
    private val cryptogramCipher: CryptogramCipher = RSACryptogramCipher()
    private val orderNumberValidator: OrderNumberValidator = OrderNumberValidator(context)
    private val cardExpiryValidator: CardExpiryValidator = CardExpiryValidator(context)
    private val cardNumberValidator: CardNumberValidator = CardNumberValidator(context)
    private val cardBindingIdValidator: CardBindingIdValidator = CardBindingIdValidator(context)
    private val cardCodeValidator: CardCodeValidator = CardCodeValidator(context)
    private val cardHolderValidator: CardHolderValidator = CardHolderValidator(context)
    private val pubKeyValidator: PubKeyValidator = PubKeyValidator(context)

    /**
     * Token generation method for a new card.
     *
     * @param params a new card information.
     * @param timestamp the timestamp used in the generated token.
     * @return generated token or error.
     */
    @JvmOverloads
    fun generateWithCard(
        params: CardParams,
        timestamp: Long = System.currentTimeMillis()
    ): TokenResult {

        val validatorsMap = mapOf(
            params.cardHolder to cardHolderValidator,
            params.mdOrder to orderNumberValidator,
            params.expiryMMYY to cardExpiryValidator,
            params.pan to cardNumberValidator,
            params.cvc to cardCodeValidator,
            params.pubKey to pubKeyValidator
        )

        val fieldErrors = mapOf(
            params.cardHolder to ParamField.CARDHOLDER,
            params.mdOrder to ParamField.MD_ORDER,
            params.expiryMMYY to ParamField.EXPIRY,
            params.pan to ParamField.PAN,
            params.cvc to ParamField.CVC,
            params.pubKey to ParamField.PUB_KEY
        )

        for ((fieldValue, validator) in validatorsMap) {
            if (fieldValue != null) {
                validator.validate(fieldValue).takeIf { !it.isValid }?.let {
                    return TokenResult.withErrors(
                        mapOf(
                            (fieldErrors[fieldValue] ?: error(ParamField.UNKNOWN)) to it.errorCode!!
                        )
                    )
                }
            }
        }

        val cardInfo = CardInfo(
            identifier = CardPanIdentifier(value = params.pan),
            cvv = params.cvc,
            expDate = params.expiryMMYY.toExpDate()
        )
        return prepareToken(params.mdOrder, cardInfo, params.pubKey, timestamp)
    }

    /**
     * Token generation method for a saved card.
     *
     * @param params information about the linked card.
     * @param timestamp the timestamp used in the generated token.
     * @return generated token or error.
     */
    @JvmOverloads
    fun generateWithBinding(
        params: BindingParams,
        timestamp: Long = System.currentTimeMillis()
    ): TokenResult {
        val validatorsMap = mapOf(
            params.mdOrder to orderNumberValidator,
            params.bindingID to cardBindingIdValidator,
            params.cvc to cardCodeValidator,
            params.pubKey to pubKeyValidator
        )

        val fieldErrors = mapOf(
            params.mdOrder to ParamField.MD_ORDER,
            params.bindingID to ParamField.BINDING_ID,
            params.cvc to ParamField.CVC,
            params.pubKey to ParamField.PUB_KEY
        )

        for ((fieldValue, validator) in validatorsMap) {
            if (fieldValue != null) {
                validator.validate(fieldValue).takeIf { !it.isValid }?.let {
                    return TokenResult.withErrors(
                        mapOf(
                            (fieldErrors[fieldValue] ?: error(ParamField.UNKNOWN)) to it.errorCode!!
                        )
                    )
                }
            }
        }

        val cardInfo = CardInfo(
            identifier = CardBindingIdIdentifier(value = params.bindingID),
            cvv = params.cvc
        )
        return prepareToken(params.mdOrder, cardInfo, params.pubKey, timestamp)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun prepareToken(
        mdOrder: String,
        cardInfo: CardInfo,
        pubKey: String,
        timestamp: Long,
    ): TokenResult {
        val paymentString = paymentStringProcessor.createPaymentString(
            order = mdOrder,
            timestamp = timestamp,
            uuid = UUID.randomUUID().toString(),
            cardInfo = cardInfo
        )
        val key = Key(
            value = pubKey,
            protocol = "RSA",
            expiration = Long.MAX_VALUE
        )
        return try {
            val token = cryptogramCipher.encode(paymentString, key)
            TokenResult.withToken(token)
        } catch (e: IllegalArgumentException) {
            TokenResult.withErrors(mapOf(ParamField.PUB_KEY to "invalid"))
        } catch (e: Exception) {
            TokenResult.withErrors(mapOf(ParamField.UNKNOWN to "unknown"))
        }
    }

    /**
     * return SDKCore version
     */
    fun getSDKVersion(): String {
        val version = BuildConfig.SDK_CORE_VERSION_NUMBER
        Log.d("SDKCore", "SDKCore version is: $version")
        return version
    }
}
