package net.payrdr.mobile.payment.sdk.form.model

import android.os.Parcel
import android.os.Parcelable
import net.payrdr.mobile.payment.sdk.form.model.Card.Companion.readCards
import net.payrdr.mobile.payment.sdk.form.model.Card.Companion.writeCards

/**
 * The result of the formation of a cryptogram.
 *
 * @param status state.
 * @param seToken generated cryptogram.
 * @param info payment method information.
 * @param deletedCardsList list of deleted cards.
 */
data class CryptogramData(
    val status: PaymentDataStatus,
    val seToken: String,
    val info: PaymentInfo? = null,
    val deletedCardsList: Set<Card> = emptySet()
) : Parcelable {

    constructor(source: Parcel) : this(
        PaymentDataStatus.values()[source.readInt()],
        source.readString()!!,
        source.readParcelable<PaymentInfo>(PaymentInfo::class.java.classLoader),
        source.readCards()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(status.ordinal)
        writeString(seToken)
        writeParcelable(info, 0)
        writeCards(deletedCardsList, flags)
    }

    companion object {

        /**
         * Object to create [CryptogramData] from data in Parcel.
         */
        @JvmField
        val CREATOR: Parcelable.Creator<CryptogramData> =
            object : Parcelable.Creator<CryptogramData> {
                override fun createFromParcel(source: Parcel): CryptogramData =
                    CryptogramData(source)

                override fun newArray(size: Int): Array<CryptogramData?> = arrayOfNulls(size)
            }
    }
}
