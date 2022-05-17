package net.payrdr.mobile.payment.sdk.core.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Card identifier.
 *
 * @param value Identifier value.
 */
sealed class CardIdentifier(open val value: String) : Parcelable {

    companion object {

        /**
         * Method for writing card id in Parcel.
         */
        fun Parcel.writeCardIdentifier(cardIdentifier: CardIdentifier, flags: Int) {
            when (cardIdentifier) {
                is CardPanIdentifier -> {
                    writeString("CardPanIdentifier")
                    writeParcelable(cardIdentifier, flags)
                }
                is CardBindingIdIdentifier -> {
                    writeString("CardBindingIdIdentifier")
                    writeParcelable(cardIdentifier, flags)
                }
            }
        }

        /**
         * Method for reading card id from Parcel.
         */
        fun Parcel.readCardIdentifier(): CardIdentifier {
            return when (readString()) {
                "CardPanIdentifier" -> readParcelable<CardPanIdentifier>(CardPanIdentifier::class.java.classLoader)!!
                "CardBindingIdIdentifier" -> readParcelable<CardBindingIdIdentifier>(
                    CardBindingIdIdentifier::class.java.classLoader
                )!!
                else -> throw IllegalArgumentException("Unknown type of card identifier for read from parcel")
            }
        }
    }
}

/**
 * Identifier by card number.
 *
 * @param value card number.
 */
data class CardPanIdentifier(override val value: String) : CardIdentifier(value) {

    constructor(source: Parcel) : this(
        source.readString()!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(value)
    }

    companion object {

        /**
         * Object to create [CardPanIdentifier] from data in Parcel.
         */
        @JvmField
        val CREATOR: Parcelable.Creator<CardPanIdentifier> =
            object : Parcelable.Creator<CardPanIdentifier> {
                override fun createFromParcel(source: Parcel): CardPanIdentifier =
                    CardPanIdentifier(source)

                override fun newArray(size: Int): Array<CardPanIdentifier?> = arrayOfNulls(size)
            }
    }
}

/**
 * Identifier by binding number.
 *
 * @param value binding number.
 */
data class CardBindingIdIdentifier(override val value: String) : CardIdentifier(value) {

    constructor(source: Parcel) : this(
        source.readString()!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(value)
    }

    companion object {

        /**
         * Object to create [CardBindingIdIdentifier] from data in Parcel.
         */
        @JvmField
        val CREATOR: Parcelable.Creator<CardBindingIdIdentifier> =
            object : Parcelable.Creator<CardBindingIdIdentifier> {
                override fun createFromParcel(source: Parcel): CardBindingIdIdentifier =
                    CardBindingIdIdentifier(source)

                override fun newArray(size: Int): Array<CardBindingIdIdentifier?> =
                    arrayOfNulls(size)
            }
    }
}
