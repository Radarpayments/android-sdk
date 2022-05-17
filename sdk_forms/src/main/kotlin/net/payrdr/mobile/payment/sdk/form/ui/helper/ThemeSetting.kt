package net.payrdr.mobile.payment.sdk.form.ui.helper

import net.payrdr.mobile.payment.sdk.form.model.Theme
import net.payrdr.mobile.payment.sdk.form.utils.defaultTheme

@Suppress("UndocumentedPublicClass")
object ThemeSetting {

    private var theme: Theme? = null

    @JvmStatic
    fun getDefaultTheme(): Theme = defaultTheme()

    @JvmStatic
    fun setTheme(theme: Theme) {
        this.theme = theme
    }

    @JvmStatic
    fun getTheme(): Theme? = theme

    fun getThemeWithDefault(default: Theme): Theme {
        return getTheme() ?: run {
            setTheme(default)
            default
        }
    }
}
