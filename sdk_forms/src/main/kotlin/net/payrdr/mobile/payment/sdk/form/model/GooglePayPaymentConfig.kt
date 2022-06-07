package net.payrdr.mobile.payment.sdk.form.model

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.wallet.PaymentDataRequest
import net.payrdr.mobile.payment.sdk.form.utils.readBooleanValue
import net.payrdr.mobile.payment.sdk.form.utils.writeBooleanValue
import java.util.Locale

/**
 * Configuring payment via the Google pay button.
 *
 * @param order order ID for payment.
 * @param uuid payment identifier.
 * @param theme customizing the interface theme.
 * @param locale the locale in which the payment form should work.
 * @param timestamp payment date.
 * @param paymentData payment information.
 * @param testEnvironment flag for making a payment in a test environment.
 */
data class GooglePayPaymentConfig internal constructor(
    val order: String = "",
    val uuid: String,
    val theme: Theme,
    val locale: Locale,
    val timestamp: Long,
    val paymentData: PaymentDataRequest,
    val testEnvironment: Boolean
) : Parcelable {

    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        Theme.values()[source.readInt()],
        source.readSerializable() as Locale,
        source.readLong(),
        source.readParcelable<PaymentDataRequest>(PaymentDataRequest::class.java.classLoader)!!,
        source.readBooleanValue()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(order)
        writeString(uuid)
        writeInt(theme.ordinal)
        writeSerializable(locale)
        writeLong(timestamp)
        writeParcelable(paymentData, flags)
        writeBooleanValue(testEnvironment)
    }

    companion object {

        /**
         * Object for creating [GooglePayPaymentConfig] from data in Parcel.
         */
        @JvmField
        val CREATOR: Parcelable.Creator<GooglePayPaymentConfig> =
            object : Parcelable.Creator<GooglePayPaymentConfig> {
                override fun createFromParcel(source: Parcel): GooglePayPaymentConfig =
                    GooglePayPaymentConfig(source)

                override fun newArray(size: Int): Array<GooglePayPaymentConfig?> =
                    arrayOfNulls(size)
            }
    }
}
