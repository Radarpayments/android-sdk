package net.payrdr.mobile.payment.sdk.form.ui.helper

import java.util.Locale

@Suppress("UndocumentedPublicClass")
object LocalizationSetting {

    private var locale: Locale? = null

    @JvmStatic
    fun getDefaultLanguage(): Locale = Locale.getDefault()

    @JvmStatic
    fun setLanguage(locale: Locale) {
        this.locale = locale
    }

    @JvmStatic
    fun getLanguage(): Locale? = locale

    fun getLanguageWithDefault(default: Locale): Locale {
        return getLanguage() ?: run {
            setLanguage(default)
            default
        }
    }
}
