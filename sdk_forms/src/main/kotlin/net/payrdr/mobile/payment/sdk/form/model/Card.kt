package net.payrdr.mobile.payment.sdk.form.model

import android.os.Parcel
import android.os.Parcelable
import net.payrdr.mobile.payment.sdk.core.model.ExpiryDate

/**
 * Description of the previously saved card.
 *
 * @param pan card number with mask.
 * @param bindingId bundle identifier.
 * @param expiryDate card expiry date.
 */
data class Card(
    val pan: String,
    val bindingId: String,
    val expiryDate: ExpiryDate? = null
) : Parcelable {

    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.readParcelable<ExpiryDate?>(ExpiryDate::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(pan)
        writeString(bindingId)
        writeParcelable(expiryDate, flags)
    }

    companion object {

        /**
         * Object to create [Card] from data in Parcel.
         */
        @JvmField
        val CREATOR: Parcelable.Creator<Card> = object : Parcelable.Creator<Card> {
            override fun createFromParcel(source: Parcel): Card = Card(source)
            override fun newArray(size: Int): Array<Card?> = arrayOfNulls(size)
        }

        /**
         * Method for reading a set of cards from Parcel.
         *
         * @return card set read.
         */
        fun Parcel.readCards(): Set<Card> {
            val size = readInt()
            return if (size > 0) {
                val cards = Array<Card?>(size) { null }
                readTypedArray(cards, CREATOR)
                cards.map { it!! }.toSet()
            } else {
                emptySet()
            }
        }

        /**
         * Method for writing a set of cards in Parcel.
         *
         * @param cards set of cards for recording.
         * @param flags recording parameters.
         */
        fun Parcel.writeCards(cards: Set<Card>, flags: Int) {
            val size = cards.size
            writeInt(size)
            if (size > 0) {
                writeTypedArray(cards.toTypedArray(), flags)
            }
        }
    }
}
