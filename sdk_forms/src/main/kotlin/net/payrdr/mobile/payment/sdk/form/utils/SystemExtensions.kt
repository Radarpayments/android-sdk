package net.payrdr.mobile.payment.sdk.form.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.model.Theme

/**
 * Method for checking if a device has a camera.
 *
 * @param context application context.
 * @return true if a camera is installed on the device, false otherwise.
 */
fun deviceHasCamera(context: Context): Boolean =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)

/**
 * Method for checking if a device has NFC.
 *
 * @param context application context.
 * @return true if the device has NFC, false otherwise.
 */
fun deviceHasNFC(context: Context): Boolean =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)

/**
 * Checks the availability of Google Play services on the device.
 *
 * @param context context.
 * @return true if Google Play services are available, otherwise false.
 */
fun deviceHasGooglePlayServices(context: Context): Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
    return resultCode == ConnectionResult.SUCCESS
}

/**
 * Displays the NFC enable prompt dialog.
 *
 * @param activity the screen on which the NFC enable request should be made.
 */
fun askToEnableNfc(activity: Activity) {
    val builder = AlertDialog.Builder(activity, R.style.PAYRDRAlertDialogTheme).apply {
        setTitle(R.string.payrdr_nfc_disabled_title)
        setMessage(R.string.payrdr_nfc_disabled_message)
        setPositiveButton(R.string.payrdr_enable) { _, _ ->
            launchNfcSettings(activity)
        }
        setNegativeButton(R.string.payrdr_cancel) { dialog, _ ->
            dialog.dismiss()
        }
    }
    builder.show()
}

/**
 * Launch the settings window for NFC control.
 *
 * @param activity is the screen where you need to open the NFC settings.
 */
fun launchNfcSettings(activity: Activity) {
    activity.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
}

/**
 * Method for defining the default theme.
 *
 * @return default theme.
 */
fun defaultTheme(): Theme = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> Theme.DEFAULT
    else -> Theme.LIGHT
}

/**
 * The method of installing the theme used.
 *
 * @param theme theme.
 */
fun AppCompatActivity.setUiTheme(theme: Theme) {
    delegate.localNightMode =
        when (theme) {
            Theme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            Theme.DEFAULT -> AppCompatDelegate.getDefaultNightMode()
        }
}
