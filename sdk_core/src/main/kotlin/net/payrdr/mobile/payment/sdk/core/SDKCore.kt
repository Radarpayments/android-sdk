package net.payrdr.mobile.payment.sdk.core

import android.content.Context
import android.util.Log
import net.payrdr.mobile.payment.sdk.core.component.CryptogramCipher
import net.payrdr.mobile.payment.sdk.core.component.PaymentStringProcessor
import net.payrdr.mobile.payment.sdk.core.component.impl.DefaultPaymentStringProcessor
import net.payrdr.mobile.payment.sdk.core.component.impl.RSACryptogramCipher
import net.payrdr.mobile.payment.sdk.core.model.BindingParams
import net.payrdr.mobile.payment.sdk.core.model.BindingInstantParams
import net.payrdr.mobile.payment.sdk.core.model.CardBindingIdIdentifier
import net.payrdr.mobile.payment.sdk.core.model.CardInfo
import net.payrdr.mobile.payment.sdk.core.model.CardPanIdentifier
import net.payrdr.mobile.payment.sdk.core.model.CardParams
import net.payrdr.mobile.payment.sdk.core.model.CardInstantParams
import net.payrdr.mobile.payment.sdk.core.model.Key
import net.payrdr.mobile.payment.sdk.core.model.MSDKRegisteredFrom
import net.payrdr.mobile.payment.sdk.core.model.NewPaymentMethodCardParams
import net.payrdr.mobile.payment.sdk.core.model.ParamField
import net.payrdr.mobile.payment.sdk.core.model.PaymentCardParams
import net.payrdr.mobile.payment.sdk.core.model.SDKCoreConfig
import net.payrdr.mobile.payment.sdk.core.model.NewPaymentMethodStoredCardParams
import net.payrdr.mobile.payment.sdk.core.utils.BindingUtils
import net.payrdr.mobile.payment.sdk.core.utils.toExpDate
import net.payrdr.mobile.payment.sdk.core.validation.BaseValidator
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
class SDKCore(
    context: Context
) {

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
     * Token generation method for a card.
     *
     * @param sdkCoreConfig sdk core configuration.
     *
     * @return generated token or error.
     */

    @Suppress("ComplexMethod")
    fun generateWithConfig(sdkCoreConfig: SDKCoreConfig): TokenResult {
        val params = sdkCoreConfig.paymentCardParams
        val timestamp = sdkCoreConfig.timestamp
        val (validatorsMap, fieldErrors) = when (params) {
            is PaymentCardParams.StoredCardPaymentParams -> {
                createValidatorAndErrorsForStoredCardPayment(params)
            }

            is PaymentCardParams.NewCardPaymentParams -> {
                createValidatorAndErrorsForNewCardPayment(params)
            }
        }

        Logger.log(
            this.javaClass,
            Logger.TAG,
            "generateWithConfig($params, $timestamp): Token generation method for a new card.",
            null
        )
        for ((fieldValue, validator) in validatorsMap) {
            Logger.log(
                this.javaClass,
                Logger.TAG,
                "generateWithConfig($params, $timestamp): Validate ${fieldErrors[fieldValue]}",
                null
            )
            if (fieldValue != null) {
                validator.validate(fieldValue).takeIf { !it.isValid }?.let {
                    Logger.log(
                        this.javaClass,
                        Logger.TAG,
                        "generateWithConfig($params, $timestamp}): Error ${fieldErrors[fieldValue]}",
                        IllegalArgumentException((fieldErrors[fieldValue] ?: ParamField.UNKNOWN).toString())
                    )
                    return TokenResult.withErrors(
                        mapOf((fieldErrors[fieldValue] ?: error(ParamField.UNKNOWN)) to it.errorCode!!)
                    )
                }
            }
        }
        val cardInfo = when (params) {
            is PaymentCardParams.StoredCardPaymentParams -> {
                createCardInfoForStoredCardPayment(params)
            }

            is PaymentCardParams.NewCardPaymentParams -> {
                createCardInfoForNewCardPayment(params)
            }
        }
        val mdOrder = when (params) {
            is CardParams -> params.mdOrder
            is NewPaymentMethodCardParams -> ""
            is BindingParams -> params.mdOrder
            is NewPaymentMethodStoredCardParams -> ""
            is CardInstantParams -> ""
            is BindingInstantParams -> ""
        }
        return prepareToken(mdOrder, cardInfo, params.pubKey, timestamp, sdkCoreConfig.registeredFrom)
    }

    private fun createCardInfoForNewCardPayment(
        paymentCardParams: PaymentCardParams.NewCardPaymentParams
    ): CardInfo {
        return CardInfo(
            identifier = CardPanIdentifier(value = paymentCardParams.pan),
            expDate = paymentCardParams.expiryMMYY.toExpDate(),
            cardHolder = paymentCardParams.cardHolder,
            cvv = paymentCardParams.cvc,
        )
    }

    private fun createCardInfoForStoredCardPayment(
        paymentCardParams: PaymentCardParams.StoredCardPaymentParams)
    : CardInfo {
        val identifier = when (paymentCardParams) {
            is BindingParams -> {
                CardBindingIdIdentifier(value = paymentCardParams.bindingID)
            }

            is NewPaymentMethodStoredCardParams -> {
                val bindingId = BindingUtils.extractBindingId(paymentCardParams.storedPaymentId)
                CardBindingIdIdentifier(value = bindingId)
            }

            is BindingInstantParams -> {
                CardBindingIdIdentifier(value = paymentCardParams.bindingID)
            }
        }
        return CardInfo(
            identifier = identifier,
            cvv = paymentCardParams.cvc,
            cardHolder = null,
        )
    }

    private fun createValidatorAndErrorsForNewCardPayment(
        paymentCardParams: PaymentCardParams.NewCardPaymentParams
    ): Pair<Map<String?, BaseValidator<String>>, Map<String?, ParamField>> {
        val validatorsMap = mutableMapOf(
            paymentCardParams.cardHolder to cardHolderValidator,
            paymentCardParams.expiryMMYY to cardExpiryValidator,
            paymentCardParams.pan to cardNumberValidator,
            paymentCardParams.cvc to cardCodeValidator,
            paymentCardParams.pubKey to pubKeyValidator
        )
        val fieldErrors = mutableMapOf(
            paymentCardParams.cardHolder to ParamField.CARDHOLDER,
            paymentCardParams.expiryMMYY to ParamField.EXPIRY,
            paymentCardParams.pan to ParamField.PAN,
            paymentCardParams.cvc to ParamField.CVC,
            paymentCardParams.pubKey to ParamField.PUB_KEY
        )
        when (paymentCardParams) {
            is CardParams -> {
                validatorsMap[paymentCardParams.mdOrder] = orderNumberValidator
                fieldErrors[paymentCardParams.mdOrder] = ParamField.MD_ORDER
            }

            is NewPaymentMethodCardParams -> {}

            is CardInstantParams -> {}
        }
        return Pair(validatorsMap.toMap(), fieldErrors.toMap())
    }

    private fun createValidatorAndErrorsForStoredCardPayment(
        paymentCardParams: PaymentCardParams.StoredCardPaymentParams
    ): Pair<Map<String?, BaseValidator<String>>, Map<String?, ParamField>> {
        val validatorsMap = mutableMapOf(
            paymentCardParams.cvc to cardCodeValidator,
            paymentCardParams.pubKey to pubKeyValidator
        )
        val fieldErrors = mutableMapOf(
            paymentCardParams.cvc to ParamField.CVC,
            paymentCardParams.pubKey to ParamField.PUB_KEY
        )
        when (paymentCardParams) {
            is BindingParams -> {
                validatorsMap[paymentCardParams.mdOrder] = orderNumberValidator
                validatorsMap[paymentCardParams.bindingID] = cardBindingIdValidator
                fieldErrors[paymentCardParams.bindingID] = ParamField.BINDING_ID
                fieldErrors[paymentCardParams.mdOrder] = ParamField.MD_ORDER
            }

            is NewPaymentMethodStoredCardParams -> {
                val bindingId = BindingUtils.extractBindingId(paymentCardParams.storedPaymentId)
                validatorsMap[bindingId] = cardBindingIdValidator
                fieldErrors[bindingId] = ParamField.STORED_PAYMENT_ID
            }

            is BindingInstantParams -> {
                validatorsMap[paymentCardParams.bindingID] = cardBindingIdValidator
                fieldErrors[paymentCardParams.bindingID] = ParamField.BINDING_ID
            }
        }
        return Pair(validatorsMap.toMap(), fieldErrors.toMap())
    }

    @Suppress("TooGenericExceptionCaught")
    private fun prepareToken(
        mdOrder: String = "",
        cardInfo: CardInfo,
        pubKey: String,
        timestamp: Long,
        registeredFrom: MSDKRegisteredFrom = MSDKRegisteredFrom.MSDK_CORE,
    ): TokenResult {

        Logger.log(this.javaClass, Logger.TAG, "generation method:", null)

        val paymentString = paymentStringProcessor.createPaymentString(
            order = mdOrder,
            timestamp = timestamp,
            uuid = UUID.randomUUID().toString(),
            cardInfo = cardInfo,
            registeredFrom = registeredFrom,
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
            Logger.log(
                this.javaClass,
                Logger.TAG, " generation method: Error",
                IllegalArgumentException("${ParamField.PUB_KEY} is invalid")
            )
            TokenResult.withErrors(mapOf(ParamField.PUB_KEY to "invalid"))
        } catch (e: Exception) {
            Logger.log(
                this.javaClass,
                Logger.TAG, " generation method: Error",
                IllegalArgumentException("${ParamField.UNKNOWN} is unknown")
            )
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
