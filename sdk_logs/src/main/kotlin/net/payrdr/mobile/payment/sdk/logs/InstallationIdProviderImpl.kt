package net.payrdr.mobile.payment.sdk.logs

import android.content.Context
import java.util.UUID

class InstallationIdProviderImpl(context: Context) : InstallationIdProvider {

    private var id: String

    init {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val installationIdFromPrefs = sharedPreferences.getString(INSTALLATION_ID_KEY, null)
        id = if (installationIdFromPrefs == null) {
            val installationId = UUID.randomUUID().toString()
            sharedPreferences.edit().putString(INSTALLATION_ID_KEY, installationId).apply()
            installationId
        } else {
            installationIdFromPrefs
        }
    }

    override fun getInstallationId(): String {
        return id
    }

    companion object {
        private const val PREFS_NAME = "installation_id_prefs"
        private const val INSTALLATION_ID_KEY = "installation_id"
    }
}