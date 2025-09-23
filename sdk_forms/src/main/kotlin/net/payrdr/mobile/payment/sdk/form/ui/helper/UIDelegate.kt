package net.payrdr.mobile.payment.sdk.form.ui.helper

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.LocaleList
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.model.Theme
import net.payrdr.mobile.payment.sdk.form.ui.helper.LocalizationSetting.getDefaultLanguage
import net.payrdr.mobile.payment.sdk.form.ui.helper.LocalizationSetting.getLanguageWithDefault
import net.payrdr.mobile.payment.sdk.form.ui.helper.ThemeSetting.getDefaultTheme
import net.payrdr.mobile.payment.sdk.form.ui.helper.ThemeSetting.getThemeWithDefault
import net.payrdr.mobile.payment.sdk.form.utils.setUiTheme
import java.util.Locale

@Suppress("TooManyFunctions")
internal open class UIDelegate(private val activity: AppCompatActivity) {
    private lateinit var currentLanguage: Locale
    private lateinit var currentTheme: Theme

    fun onCreate(bundle: Bundle?) {
        bundle?.apply {
            getSerializable(BUNDLE_LANGUAGE)?.let {
                LocalizationSetting.setLanguage(it as Locale)
            }
            getSerializable(BUNDLE_THEME)?.let {
                ThemeSetting.setTheme(it as Theme)
            }
        }
        setup(true)
        enableEdgeToEdge()
    }

    fun updateConfiguration(context: Context) {
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(
            context.resources.configuration,
            context.resources.displayMetrics
        )
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            putSerializable(BUNDLE_LANGUAGE, currentLanguage)
            putSerializable(BUNDLE_THEME, currentTheme)
        }
    }

    fun onResume() {
        Handler().post {
            checkSettingsChanged(false)
        }
    }

    fun attachBaseContext(context: Context): Context {
        val locale = getLanguageWithDefault(
            getDefaultLanguage()
        )
        val config = context.resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            config.setLocale(locale)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
            context.createConfigurationContext(config)
        } else context
    }

    fun getApplicationContext(applicationContext: Context): Context {
        val baseLocale = getLocaleFromConfiguration(applicationContext.resources.configuration)
        val currentLocale = getLanguageWithDefault(
            getDefaultLanguage()
        )
        return if (!baseLocale.toString().equals(currentLocale.toString(), ignoreCase = true)) {
            val context = LocalizationContext(applicationContext)
            val config = context.resources.configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                config.setLocale(currentLocale)
                val localeList = LocaleList(currentLocale)
                LocaleList.setDefault(localeList)
                config.setLocales(localeList)
                context.createConfigurationContext(config)
            } else {
                @Suppress("DEPRECATION")
                config.locale = currentLocale
                @Suppress("DEPRECATION")
                context.resources.updateConfiguration(
                    config,
                    context.resources.displayMetrics
                )
                context
            }
        } else {
            applicationContext
        }
    }

    @Suppress("DEPRECATION")
    fun getLocaleFromConfiguration(configuration: Configuration): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            configuration.locales.get(0)
        } else {
            configuration.locale
        }
    }

    @Suppress("DEPRECATION")
    fun getResources(resources: Resources): Resources {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val locale = LocalizationSetting.getLanguage()
            val config = resources.configuration
            config.setLocale(locale)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
            resources
        } else {
            val config = resources.configuration
            config.locale = LocalizationSetting.getLanguage()
            val metrics = resources.displayMetrics
            Resources(activity.assets, metrics, config)
        }
    }

    private fun setup(isCreate: Boolean) {
        LocalizationSetting.getLanguage()?.let { locale -> currentLanguage = locale }
        ThemeSetting.getTheme()?.let { theme -> currentTheme = theme }
        checkSettingsChanged(isCreate)
    }

    private fun isCurrentLanguageSetting(newLocale: Locale, currentLocale: Locale): Boolean {
        return newLocale.toString() == currentLocale.toString()
    }

    private fun isCurrentThemeSetting(newTheme: Theme, currentTheme: Theme): Boolean {
        return newTheme == currentTheme
    }

    private fun checkSettingsChanged(isCreate: Boolean) {
        val oldLanguage = getLanguageWithDefault(getDefaultLanguage())
        val oldTheme = getThemeWithDefault(getDefaultTheme())
        val localeChanged = !isCurrentLanguageSetting(currentLanguage, oldLanguage)
        val themeChanged = !isCurrentThemeSetting(currentTheme, oldTheme)
        if (localeChanged || themeChanged || isCreate) {
            if (themeChanged || isCreate) {
                activity.window.setWindowAnimations(R.style.PAYRDRWindowAnimationFadeInOut)
                activity.setUiTheme(currentTheme)
                supportSystemBarColor()
            } else if (localeChanged && !(themeChanged || isCreate)) {
                activity.recreate()
            }
        }
    }

    private fun enableEdgeToEdge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30
            activity.window.setDecorFitsSystemWindows(false)
        } else {
            val decorFitsFlags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            val decorView: View = activity.window.decorView
            val sysUiVis = decorView.systemUiVisibility
            decorView.systemUiVisibility = sysUiVis or decorFitsFlags
        }

        // API 29
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.window.isNavigationBarContrastEnforced = false
        }
    }

    private fun supportSystemBarColor() {
        // true = dark icons
        val isLightBars = shouldUseLightBars(currentTheme)
        when {
            // API 30
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                activity.window.decorView.post {
                    val c = activity.window.insetsController
                        ?: activity.window.decorView.windowInsetsController
                    val mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    if (isLightBars) c?.setSystemBarsAppearance(mask, mask)
                    else c?.setSystemBarsAppearance(0, mask)
                }
            }

            // API 26
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                val decor = activity.window.decorView
                decor.post {
                    val flags = if (isLightBars) {
                        decor.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    } else {
                        decor.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() and
                                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                    }
                    decor.systemUiVisibility = flags
                }
            }

            // API 23
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {

                activity.window.navigationBarColor =
                    activity.resources.getColor(R.color.color_nav_bar_old_android)

                val decor = activity.window.decorView
                decor.post {
                    val flags = if (isLightBars) {
                        decor.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    } else {
                        decor.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                    }
                    decor.systemUiVisibility = flags
                }
            }

            else -> {
                // system bar colors cant be changed for API < 23,
                // so we set color to statusBar not transparent
                val color = if (isLightBars) Color.BLACK else Color.WHITE
                activity.window.statusBarColor = color
                activity.window.navigationBarColor = color
            }
        }
    }

    private fun shouldUseLightBars(theme: Theme): Boolean = when (theme) {
        Theme.DEFAULT -> isDarkTheme(activity.resources).not()
        Theme.LIGHT -> true
        Theme.DARK -> false
        Theme.SYSTEM -> isDarkTheme(activity.resources).not()
    }

    companion object {
        private const val BUNDLE_LANGUAGE = "payment.sdk.settings.language"
        private const val BUNDLE_THEME = "payment.sdk.settings.theme"

        fun isDarkTheme(resources: Resources): Boolean {
            val currentNightMode =
                resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return currentNightMode == Configuration.UI_MODE_NIGHT_YES
        }
    }
}
