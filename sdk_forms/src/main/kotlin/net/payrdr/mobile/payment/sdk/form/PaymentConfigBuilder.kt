package net.payrdr.mobile.payment.sdk.form

import net.payrdr.mobile.payment.sdk.core.Logger
import net.payrdr.mobile.payment.sdk.core.model.MSDKRegisteredFrom
import net.payrdr.mobile.payment.sdk.form.model.AdditionalField
import net.payrdr.mobile.payment.sdk.form.model.CameraScannerOptions
import net.payrdr.mobile.payment.sdk.form.model.Card
import net.payrdr.mobile.payment.sdk.form.model.CardDeleteOptions
import net.payrdr.mobile.payment.sdk.form.model.CardSaveOptions
import net.payrdr.mobile.payment.sdk.form.model.HolderInputOptions
import net.payrdr.mobile.payment.sdk.form.model.NfcScannerOptions
import net.payrdr.mobile.payment.sdk.form.model.PaymentConfig
import net.payrdr.mobile.payment.sdk.form.model.Theme
import java.util.Locale
import java.util.UUID

/**
 * Constructor for the formation of the configuration of the payment.
 *
 * @param order identifier of the paid order.
 */
@Suppress("TooManyFunctions")
class PaymentConfigBuilder(private val order: String = "") {
    private var buttonText: String? = null
    private var cards: Set<Card> = emptySet()
    private var cardSaveOptions: CardSaveOptions = CardSaveOptions.HIDE
    private var holderInputOptions: HolderInputOptions = HolderInputOptions.HIDE
    private var cameraScannerOptions: CameraScannerOptions = CameraScannerOptions.ENABLED
    private var theme: Theme = Theme.DEFAULT
    private var nfcScannerOptions: NfcScannerOptions = NfcScannerOptions.ENABLED
    private var uuid: String = UUID.randomUUID().toString()
    private var timestamp: Long = System.currentTimeMillis()
    private var locale: Locale = Locale.getDefault()
    private var storedPaymentMethodCVCRequired: Boolean = true
    private var cardDeleteOptions: CardDeleteOptions = CardDeleteOptions.NO_DELETE
    private var registeredFrom: MSDKRegisteredFrom = MSDKRegisteredFrom.MSDK_FORMS
    private var paramsNeedToBeFilledForMastercard: List<AdditionalField> = emptyList()
    private var paramsNeedToBeFilledForVisa: List<AdditionalField> = emptyList()

    /**
     * Change the text of the payment button.
     *
     * Optional, by default localized translation "Pay"
     *
     * @param buttonText the text of the payment button.
     * @return the current constructor.
     */
    fun buttonText(buttonText: String): PaymentConfigBuilder = apply {
        Logger.log(
            this.javaClass,
            Constants.TAG,
            "buttonText($buttonText): Change the text of the payment button.",
            null
        )
        this.buttonText = buttonText
    }

    /**
     * Adding a list of linked cards.
     *
     * Optional, default empty list.
     *
     * @param cards list of related cards.
     * @return the current constructor.
     */
    fun cards(cards: Set<Card>): PaymentConfigBuilder = apply {
        Logger.log(
            this.javaClass,
            Constants.TAG,
            "cards($cards): Adding a list of linked cards.",
            null
        )
        this.cards = cards
    }

    /**
     * Option to manage the ability to bind a new card.
     *
     * Optional, default HIDE
     *
     * @param options setting the function of binding a new card.
     * @return the current constructor.
     */
    fun cardSaveOptions(options: CardSaveOptions): PaymentConfigBuilder = apply {
        Logger.log(
            this.javaClass,
            Constants.TAG,
            "cardSaveOptions($options): Option to manage the ability to bind a new card.",
            null
        )
        this.cardSaveOptions = options
    }

    /**
     * Option to control the functionality of scanning the card through the camera.
     *
     * Optional, default ENABLED
     *
     * @param options setting of the card scanning function.
     * @return the current constructor.
     */
    fun cameraScannerOptions(options: CameraScannerOptions): PaymentConfigBuilder = apply {
        Logger.log(
            this.javaClass,
            Constants.TAG,
            "cameraScannerOptions($options):",
            null
        )
        this.cameraScannerOptions = options
    }

    /**
     * Option to control the functionality of card scanning via NFC.
     *
     * Optional, default ENABLED
     *
     * @param options setting of the card scanning function.
     * @return the current constructor.
     */
    fun nfcScannerOptions(options: NfcScannerOptions): PaymentConfigBuilder = apply {
        Logger.log(
            this.javaClass,
            Constants.TAG,
            "nfcScannerOptions($options): Option to control the functionality of card scanning via NFC.",
            null
        )
        this.nfcScannerOptions = options
    }

    /**
     * Option to control the theme of the interface.
     *
     * Optional, default SYSTEM.
     *
     * @param theme setting of the card scanning function.
     * @return the current constructor.
     */
    fun theme(theme: Theme): PaymentConfigBuilder = apply {
        Logger.log(
            this.javaClass,
            Constants.TAG,
            "theme($theme): Option to control the theme of the interface.",
            null
        )
        this.theme = theme
    }

    /**
     * Option to control the ability to display the cardholder input field.
     *
     * Optional, default HIDE
     *
     * @param options setting the cardholder input field.
     * @return the current constructor.
     */
    fun holderInputOptions(options: HolderInputOptions): PaymentConfigBuilder = apply {
        Logger.log(
            this.javaClass,
            Constants.TAG,
            "holderInputOptions($options):",
            null
        )
        this.holderInputOptions = options
    }

    /**
     * Setting a unique identifier for the payment.
     *
     * Optionally, a unique payment identifier is generated automatically.
     *
     * @param uuid payment identifier.
     * @return the current constructor.
     */
    fun uuid(uuid: String): PaymentConfigBuilder = apply {
        Logger.log(
            this.javaClass,
            Constants.TAG,
            "uuid($uuid): Setting a unique identifier for the payment.",
            null
        )
        this.uuid = uuid
    }

    /**
     * Setting the time of formation of payment.
     *
     * Optionally, the time of formation of the payment is set automatically.
     *
     * @param timestamp time of payment formation.
     * @return the current constructor.
     */
    fun timestamp(timestamp: Long): PaymentConfigBuilder = apply {
        Logger.log(
            this.javaClass,
            Constants.TAG,
            "timestamp($timestamp): Setting the time of formation of payment.",
            null
        )
        this.timestamp = timestamp
    }

    /**
     * Installation of localization.
     *
     * Optionally, the localization of the shape of the floor is determined automatically.
     *
     * @param locale localization.
     * @return the current constructor.
     */
    fun locale(locale: Locale): PaymentConfigBuilder = apply {
        Logger.log(
            this.javaClass,
            Constants.TAG,
            "locale($locale): Installation of localization.",
            null
        )
        this.locale = locale
    }

    /**
     * Setting the check for mandatory filling of the CVC field when paying with a linked card.
     *
     * Optional, default true.
     *
     * @param required CVC filling requirement.
     * @return the current constructor.
     */
    fun storedPaymentMethodCVCRequired(required: Boolean): PaymentConfigBuilder = apply {
        Logger.log(
            this.javaClass,
            Constants.TAG,
            "storedPaymentMethodCVCRequired($required):",
            null
        )
        this.storedPaymentMethodCVCRequired = required
    }

    /**
     * Option to manage the ability to remove the card.
     *
     * Optional, default NO_DELETE
     *
     * @param options setting the function for deleting the map.
     * @return the current constructor.
     */
    fun cardDeleteOptions(options: CardDeleteOptions): PaymentConfigBuilder = apply {
        Logger.log(
            this.javaClass,
            Constants.TAG,
            "cardDeleteOptions($options): Option to manage the ability to remove the card.",
            null
        )
        this.cardDeleteOptions = options
    }

    /**
     * Option to manage the source.
     *
     * Optional, default MSDK_FORMS
     *
     * @param registeredFrom setting the source.
     * @return the current constructor.
     */
    fun registeredFrom(registeredFrom: MSDKRegisteredFrom): PaymentConfigBuilder = apply {
        Logger.log(
            this.javaClass,
            Constants.TAG,
            "registeredFrom($registeredFrom): Option to manage the source.",
            null
        )
        this.registeredFrom = registeredFrom
    }

    /**
     * Option to add additional fields about payer to fill by payer when pay with MASTERCARD.
     *
     * Optional, default empty.
     *
     * @param paramsNeedToBeFilledForMastercard the list of additional fields.
     * @return the current constructor.
     */
    fun paramsNeedToBeFilledForMastercard(
        paramsNeedToBeFilledForMastercard: List<AdditionalField>
    ): PaymentConfigBuilder = apply {
        this.paramsNeedToBeFilledForMastercard = paramsNeedToBeFilledForMastercard
    }

    /**
     * Option to add additional fields about payer to fill by payer when pay with VISA.
     *
     * Optional, default empty.
     *
     * @param paramsNeedToBeFilledForVisa the list of additional fields.
     * @return the current constructor.
     */
    fun paramsNeedToBeFilledForVisa(
        paramsNeedToBeFilledForVisa: List<AdditionalField>
    ): PaymentConfigBuilder = apply {
        this.paramsNeedToBeFilledForVisa = paramsNeedToBeFilledForVisa
    }

    /**
     * Creates a payment configuration.
     *
     * @return payment configuration.
     */
    fun build() = PaymentConfig(
        order = this.order,
        cardSaveOptions = this.cardSaveOptions,
        holderInputOptions = this.holderInputOptions,
        cameraScannerOptions = this.cameraScannerOptions,
        theme = this.theme,
        nfcScannerOptions = this.nfcScannerOptions,
        cards = this.cards,
        uuid = this.uuid,
        timestamp = this.timestamp,
        buttonText = this.buttonText,
        locale = this.locale,
        storedPaymentMethodCVCRequired = this.storedPaymentMethodCVCRequired,
        cardDeleteOptions = this.cardDeleteOptions,
        registeredFrom = this.registeredFrom,
        fieldsNeedToBeFilledForMastercard = this.paramsNeedToBeFilledForMastercard,
        fieldsNeedToBeFilledForVisa = this.paramsNeedToBeFilledForVisa
    )
}
