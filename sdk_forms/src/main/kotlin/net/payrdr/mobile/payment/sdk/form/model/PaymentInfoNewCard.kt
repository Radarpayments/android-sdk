package net.payrdr.mobile.payment.sdk.form.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Data on payment with a new card.
 *
 * @param order identifier of the paid order.
 * @param saveCard user choice - true if he wants to save the card, otherwise false.
 * @param holder is the specified name of the card holder.
 */
data class PaymentInfoNewCard(
    val order: String,
    val saveCard: Boolean,
    val holder: String
) : PaymentInfo, Parcelable {

    constructor(source: Parcel) : this(
        source.readString()!!,
        1 == source.readInt(),
        source.readString()!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(order)
        writeInt((if (saveCard) 1 else 0))
        writeString(holder)
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
