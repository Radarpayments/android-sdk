package net.payrdr.mobile.payment.sdk.logs

fun interface InstallationIdProvider {
    fun getInstallationId():String
}