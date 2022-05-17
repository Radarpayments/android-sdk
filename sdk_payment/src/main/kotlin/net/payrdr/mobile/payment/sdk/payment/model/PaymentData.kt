package net.payrdr.mobile.payment.sdk.payment.model

import android.os.Parcel
import android.os.Parcelable

/**
 * The result of a full payment cycle.
 *
 * @param mdOrder order number.
 * @param status payment status.
 */
data class PaymentData(
    val mdOrder: String?,
    val status: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mdOrder)
        parcel.writeString(status)
    }

    override fun describeContents(): Int {
        return 0
    }

    /**
     * Object to create [PaymentData] from data in Parcel.
     */
    companion object CREATOR : Parcelable.Creator<PaymentData> {
        override fun createFromParcel(parcel: Parcel): PaymentData =
            PaymentData(parcel)

        override fun newArray(size: Int): Array<PaymentData?> = arrayOfNulls(size)
    }
}
