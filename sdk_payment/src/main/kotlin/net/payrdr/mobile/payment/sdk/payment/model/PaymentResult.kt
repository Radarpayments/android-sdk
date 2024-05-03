package net.payrdr.mobile.payment.sdk.payment.model

import android.os.Parcel
import android.os.Parcelable
import net.payrdr.mobile.payment.sdk.form.SDKException

/**
 * The result of a full payment cycle.
 *
 * @param mdOrder order number.
 * @param isSuccess the payment process was completed successfully.
 * @param exception possible exception for order payment error.
 */
data class PaymentResult(
    val mdOrder: String,
    val isSuccess: Boolean,
    val exception: SDKException?,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt() == 1,
        parcel.readSerializable() as SDKException?,
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mdOrder)
        parcel.writeInt(if (isSuccess) 1 else 0)
        parcel.writeSerializable(exception)
    }

    override fun describeContents(): Int {
        return 0
    }

    /**
     * Object to create [PaymentResult] from data in Parcel.
     */
    companion object CREATOR : Parcelable.Creator<PaymentResult> {
        override fun createFromParcel(parcel: Parcel): PaymentResult =
            PaymentResult(parcel)

        override fun newArray(size: Int): Array<PaymentResult?> = arrayOfNulls(size)
    }
}
