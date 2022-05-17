package net.payrdr.mobile.payment.sdk.form.ui.helper

import java.util.Locale

/**
 * Available localizations in which the payment functionality can work.
 */
object Locales {

    /**
     * English.
     */
    fun english(): Locale = Locale.ENGLISH

    /**
     * Russian.
     */
    fun russian(): Locale = Locale.Builder().setLanguage("ru").build()

    /**
     * Ukraine.
     */
    fun ukrainian(): Locale = Locale.Builder().setLanguage("uk").build()

    /**
     * German.
     */
    fun german(): Locale = Locale.Builder().setLanguage("de").build()

    /**
     * Spanish.
     */
    fun spanish(): Locale = Locale.Builder().setLanguage("es").build()

    /**
     * French.
     */
    fun french(): Locale = Locale.Builder().setLanguage("fr").build()

    /**
     * All available localizations of the form of payment.
     */
    fun availableLocales() = listOf(
        russian(),
        english(),
        ukrainian(),
        french(),
        spanish(),
        german()
    )
}
