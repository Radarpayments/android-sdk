package net.payrdr.mobile.payment.sdk.form

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
class PaymentConfigBuilder(private val order: String) {
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
    private var bindingCVCRequired: Boolean = true
    private var cardDeleteOptions: CardDeleteOptions = CardDeleteOptions.NO_DELETE

    /**
     * Change the text of the payment button.
     *
     * Optional, by default localized translation "Pay"
     *
     * @param buttonText the text of the payment button.
     * @return the current constructor.
     */
    fun buttonText(buttonText: String): PaymentConfigBuilder = apply {
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
    fun bindingCVCRequired(required: Boolean): PaymentConfigBuilder = apply {
        this.bindingCVCRequired = required
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
        this.cardDeleteOptions = options
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
        bindingCVCRequired = this.bindingCVCRequired,
        cardDeleteOptions = this.cardDeleteOptions
    )
}
