package net.payrdr.mobile.payment.sdk.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Google Pay payment data.
 *
 * @param order identifier of the paid order.
 * @param paymentToken identifier of payment.
 * @param email payer email from Google Pay.
 * @param phone payer phone number from Google Pay.
 */
data class PaymentInfoGooglePay(
    val order: String,
    val paymentToken: String,
    val email: String? = null,
    val phone: String? = null
) : PaymentInfo {

    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.readString(),
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(order)
        writeString(paymentToken)
        writeString(email)
        writeString(phone)
    }

    companion object {

        /**
         * Object to create [PaymentInfoGooglePay] from data in Parcel.
         */
        @JvmField
        val CREATOR: Parcelable.Creator<PaymentInfoGooglePay> =
            object : Parcelable.Creator<PaymentInfoGooglePay> {
                override fun createFromParcel(source: Parcel): PaymentInfoGooglePay =
                    PaymentInfoGooglePay(source)

                override fun newArray(size: Int): Array<PaymentInfoGooglePay?> = arrayOfNulls(size)
            }
    }
}
