package net.payrdr.mobile.payment.sdk.core.model

import android.os.Parcel
import android.os.Parcelable
import net.payrdr.mobile.payment.sdk.core.model.CardIdentifier.Companion.readCardIdentifier
import net.payrdr.mobile.payment.sdk.core.model.CardIdentifier.Companion.writeCardIdentifier

/**
 * Data class for card information used for payment.
 *
 * @param identifier card identifier.
 * @param expDate expiration date of the card.
 * @param cvv security code.
 */
data class CardInfo(
    val identifier: CardIdentifier,
    val expDate: ExpiryDate? = null,
    val cvv: String? = null
) : Parcelable {

    constructor(source: Parcel) : this(
        source.readCardIdentifier(),
        source.readParcelable<ExpiryDate?>(ExpiryDate::class.java.classLoader),
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeCardIdentifier(identifier, flags)
        writeParcelable(expDate, flags)
        writeValue(cvv)
    }

    companion object {

        /**
         * Object to create [CardInfo] from data in Parcel.
         */
        @JvmField
        val CREATOR: Parcelable.Creator<CardInfo> = object : Parcelable.Creator<CardInfo> {
            override fun createFromParcel(source: Parcel): CardInfo = CardInfo(source)
            override fun newArray(size: Int): Array<CardInfo?> = arrayOfNulls(size)
        }
    }
}
