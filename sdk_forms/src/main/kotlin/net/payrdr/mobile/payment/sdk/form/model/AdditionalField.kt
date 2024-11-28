package net.payrdr.mobile.payment.sdk.form.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Describes information to fill by payer about additional card field for VISA and MASTERCARD payment systems.
 * @param fieldName - name of additional field.
 * @param prefilledValue - prefilled value for this field.
 * @param isMandatory - required to fill or not.
 */
data class AdditionalField(
    val fieldName: String,
    val prefilledValue: String?,
    val isMandatory: Boolean,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(fieldName)
        parcel.writeString(prefilledValue)
        parcel.writeByte(if (isMandatory) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    /**
     * Object to create [AdditionalField] from data in Parcel.
     */
    companion object CREATOR : Parcelable.Creator<AdditionalField> {
        override fun createFromParcel(parcel: Parcel): AdditionalField {
            return AdditionalField(parcel)
        }

        override fun newArray(size: Int): Array<AdditionalField?> {
            return arrayOfNulls(size)
        }
    }
}
