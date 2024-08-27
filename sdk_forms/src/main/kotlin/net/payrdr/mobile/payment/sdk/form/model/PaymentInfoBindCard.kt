package net.payrdr.mobile.payment.sdk.form.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Payment data for the linked card.
 *
 * @param order identifier of the paid order.
 * @param bindingId ID of the associated card used for payment.
 * @param cvc payment card cvc.
 * @param filledAdditionalPayerParams filled additional information about payer.
 */
data class PaymentInfoBindCard(
    val order: String = "",
    val bindingId: String,
    val cvc: String,
    val filledAdditionalPayerParams: FilledAdditionalPayerParams
) : PaymentInfo {

    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readParcelable(FilledAdditionalPayerParams::class.java.classLoader)!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(order)
        writeString(bindingId)
        writeString(cvc)
        writeParcelable(filledAdditionalPayerParams, flags)
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
