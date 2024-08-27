package net.payrdr.mobile.payment.sdk.form.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Data on payment with a new card.
 *
 * @param order identifier of the paid order.
 * @param saveCard user choice - true if he wants to save the card, otherwise false.
 * @param holder is the specified name of the card holder.
 * @param pan payment card pan.
 * @param cvc payment card cvc.
 * @param expiryDate payment card expiry date.
 * @param filledAdditionalPayerParams filled additional information about payer.
 */
data class PaymentInfoNewCard(
    val order: String = "",
    val saveCard: Boolean,
    val holder: String,
    val pan: String,
    val cvc: String,
    val expiryDate: String,
    val filledAdditionalPayerParams: FilledAdditionalPayerParams
) : PaymentInfo {

    constructor(source: Parcel) : this(
        source.readString()!!,
        1 == source.readInt(),
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readParcelable(FilledAdditionalPayerParams::class.java.classLoader)!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(order)
        writeInt((if (saveCard) 1 else 0))
        writeString(holder)
        writeString(pan)
        writeString(cvc)
        writeString(expiryDate)
        writeParcelable(filledAdditionalPayerParams, flags)
    }

    companion object {

        /**
         * An object to create a [PaymentInfoNewCard] from data in Parcel.
         */
        @JvmField
        val CREATOR: Parcelable.Creator<PaymentInfoNewCard> =
            object : Parcelable.Creator<PaymentInfoNewCard> {
                override fun createFromParcel(source: Parcel): PaymentInfoNewCard =
                    PaymentInfoNewCard(source)

                override fun newArray(size: Int): Array<PaymentInfoNewCard?> = arrayOfNulls(size)
            }
    }
}
