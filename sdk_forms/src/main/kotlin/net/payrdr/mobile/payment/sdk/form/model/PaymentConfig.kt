package net.payrdr.mobile.payment.sdk.form.model

import android.os.Parcel
import android.os.Parcelable
import net.payrdr.mobile.payment.sdk.core.model.MSDKRegisteredFrom
import net.payrdr.mobile.payment.sdk.form.model.Card.Companion.readCards
import net.payrdr.mobile.payment.sdk.form.model.Card.Companion.writeCards
import java.util.Locale

/**
 * Payment configuration.
 *
 * @param order order ID for payment .
 * @param cardSaveOptions setting the option to link a new card after payment.
 * @param holderInputOptions setting the cardholder input option.
 * @param cameraScannerOptions setting the option to scan the card data through the camera.
 * @param nfcScannerOptions setting option to scan card data via NFC.
 * @param theme customize the interface theme.
 * @param cards list of linked cards.
 * @param uuid payment id.
 * @param timestamp time of payment.
 * @param locale locale in which the payment form should work.
 * @param buttonText the text of the payment button.
 * @param storedPaymentMethodCVCRequired mandatory entry of CVC paying with a previously saved card.
 * @param cardDeleteOptions the option to remove the card.
 * @param registeredFrom source of token generation.
 * @param fieldsNeedToBeFilledForMastercard the list of additional fields about payer to fill when pay by MASTERCARD.
 * @param fieldsNeedToBeFilledForVisa the list of additional fields about payer to fill when pay by VISA.
 */
data class PaymentConfig internal constructor(
    val order: String = "",
    val cardSaveOptions: CardSaveOptions,
    val holderInputOptions: HolderInputOptions,
    val cameraScannerOptions: CameraScannerOptions,
    val theme: Theme,
    val nfcScannerOptions: NfcScannerOptions,
    val cards: Set<Card>,
    val uuid: String,
    val timestamp: Long,
    val locale: Locale,
    val buttonText: String?,
    val storedPaymentMethodCVCRequired: Boolean,
    val cardDeleteOptions: CardDeleteOptions,
    val registeredFrom: MSDKRegisteredFrom,
    val fieldsNeedToBeFilledForMastercard: List<AdditionalField>,
    val fieldsNeedToBeFilledForVisa:List<AdditionalField>
) : Parcelable {

    constructor(source: Parcel) : this(
        source.readString()!!,
        CardSaveOptions.values()[source.readInt()],
        HolderInputOptions.values()[source.readInt()],
        CameraScannerOptions.values()[source.readInt()],
        Theme.values()[source.readInt()],
        NfcScannerOptions.values()[source.readInt()],
        source.readCards(),
        source.readString()!!,
        source.readLong(),
        source.readSerializable() as Locale,
        source.readString(),
        1 == source.readInt(),
        CardDeleteOptions.values()[source.readInt()],
        source.readString()?.let {  MSDKRegisteredFrom.valueOf(it) } ?: MSDKRegisteredFrom.MSDK_CORE,
        source.createTypedArrayList(AdditionalField.CREATOR) ?: emptyList(),
        source.createTypedArrayList(AdditionalField.CREATOR) ?: emptyList()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(order)
        writeInt(cardSaveOptions.ordinal)
        writeInt(holderInputOptions.ordinal)
        writeInt(cameraScannerOptions.ordinal)
        writeInt(theme.ordinal)
        writeInt(nfcScannerOptions.ordinal)
        writeCards(cards, flags)
        writeString(uuid)
        writeLong(timestamp)
        writeSerializable(locale)
        writeString(buttonText)
        writeInt((if (storedPaymentMethodCVCRequired) 1 else 0))
        writeInt(cardDeleteOptions.ordinal)
        writeString(registeredFrom.registeredFromValue)
        writeTypedList(fieldsNeedToBeFilledForMastercard)
        writeTypedList(fieldsNeedToBeFilledForVisa)
    }

    companion object {

        /**
         * Object to create [PaymentConfig] from data in Parcel.
         */
        @JvmField
        val CREATOR: Parcelable.Creator<PaymentConfig> =
            object : Parcelable.Creator<PaymentConfig> {
                override fun createFromParcel(source: Parcel): PaymentConfig = PaymentConfig(source)
                override fun newArray(size: Int): Array<PaymentConfig?> = arrayOfNulls(size)
            }
    }
}
