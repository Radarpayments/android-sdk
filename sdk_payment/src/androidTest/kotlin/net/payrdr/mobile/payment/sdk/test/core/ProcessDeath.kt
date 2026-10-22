package net.payrdr.mobile.payment.sdk.test.core

import net.payrdr.mobile.payment.sdk.SDKForms
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.ui.helper.LocalizationSetting
import net.payrdr.mobile.payment.sdk.ui.helper.ThemeSetting

/**
 * Simulates Android process death for SDK singletons: in-memory state is lost,
 * while everything persisted on disk survives, as in a real process restart.
 *
 * Combine with ActivityScenario.recreate() to also get the saved instance state.
 */
object ProcessDeath {

    fun simulate() {
        resetStaticField(SDKPayment::class.java, "innerSdkPaymentConfig")
        SDKForms.innerSdkConfig = null
        SDKForms.deleteCardHandler = null
        resetStaticField(ThemeSetting::class.java, "theme")
        resetStaticField(LocalizationSetting::class.java, "locale")
    }

    private fun resetStaticField(owner: Class<*>, name: String) {
        owner.getDeclaredField(name).apply {
            isAccessible = true
            set(null, null)
        }
    }
}
