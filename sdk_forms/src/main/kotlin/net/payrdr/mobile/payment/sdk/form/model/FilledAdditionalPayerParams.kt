package net.payrdr.mobile.payment.sdk.form.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Contain information filled by payer about himself.
 * @param city payer city.
 * @param country payer country.
 * @param postalCode payer postal code.
 * @param state payer state.
 * @param addressLine1  * payer address details 1.
 * @param addressLine2 payer address details 2.
 * @param addressLine3 payer address details 3.
 * @param phone payer mobile phone.
 * @param email payer email.
 */
data class FilledAdditionalPayerParams(
    val city: String?,
    val country: String?,
    val postalCode: String?,
    val state: String?,
    val addressLine1: String?,
    val addressLine2: String?,
    val addressLine3: String?,
    val phone: String?,
    val email: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(city)
        parcel.writeString(country)
        parcel.writeString(postalCode)
        parcel.writeString(state)
        parcel.writeString(addressLine1)
        parcel.writeString(addressLine2)
        parcel.writeString(addressLine3)
        parcel.writeString(phone)
        parcel.writeString(email)
    }

    override fun describeContents(): Int {
        return 0
    }

    /**
     * Object to create [FilledAdditionalPayerParams] from Parcel.
     */
    companion object CREATOR : Parcelable.Creator<FilledAdditionalPayerParams> {
        override fun createFromParcel(parcel: Parcel): FilledAdditionalPayerParams {
            return FilledAdditionalPayerParams(parcel)
        }

        override fun newArray(size: Int): Array<FilledAdditionalPayerParams?> {
            return arrayOfNulls(size)
        }
    }
}
