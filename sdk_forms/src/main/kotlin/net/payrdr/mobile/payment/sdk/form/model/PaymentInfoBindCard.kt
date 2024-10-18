package net.payrdr.mobile.payment.sdk.form.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Payment data for the linked card.
 *
 * @param order identifier of the paid order.
 * @param bindingId ID of the associated card used for payment.
 */
data class PaymentInfoBindCard(
    val order: String = "",
    val bindingId: String
) : PaymentInfo {

    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(order)
        writeString(bindingId)
    }

    companion object {

        /**
         * An object to create a [PaymentInfoBindCard] from data in Parcel.
         */
        @JvmField
        val CREATOR: Parcelable.Creator<PaymentInfoBindCard> =
            object : Parcelable.Creator<PaymentInfoBindCard> {
                override fun createFromParcel(source: Parcel): PaymentInfoBindCard =
                    PaymentInfoBindCard(source)

                override fun newArray(size: Int): Array<PaymentInfoBindCard?> = arrayOfNulls(size)
            }
    }
}
