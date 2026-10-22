package net.payrdr.mobile.payment.sdk.ui.helper

import android.content.Context

internal object CardLogoAssetsResolver {

    fun resolveByPan(context: Context, pan: String, preferLight: Boolean = false): String? =
        CardNetworkResolver.resolve(pan).logoName?.let { resolveByName(context, it, preferLight) }

    fun resolveByName(context: Context, system: String, preferLight: Boolean = false): String? {
        val assetsFolder = "payment_system"
        val iconsList = context.resources.assets.list(assetsFolder)
        return if (preferLight && iconsList?.contains("logo-$system-white.svg") == true) {
            "$assetsFolder/logo-$system-white.svg"
        } else if (iconsList?.contains("logo-$system.svg") == true) {
            "$assetsFolder/logo-$system.svg"
        } else {
            null
        }
    }
}
