package net.payrdr.mobile.payment.sdk.form.utils

import android.os.Parcel

/**
 * Extension to read Boolean value from Parcel.
 *
 * The Parcel.readByte () method is used to get the Bool value.
 *
 * @return true if the value 1 was read, otherwise false.
 */
fun Parcel.readBooleanValue(): Boolean = readByte() == 1.toByte()

/**
 * Extension for writing Boolean values in Parcel.
 *
 * The Parcel.writeByte () method is used to write the Bool value. To write true is used
 * value 1, to write false value 0.
 *
 * @param value Boolean value to write to Parcel.
 */
fun Parcel.writeBooleanValue(value: Boolean) {
    writeByte(if (value) 1 else 0)
}
