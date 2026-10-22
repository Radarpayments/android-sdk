package net.payrdr.mobile.payment.sdk.ui

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import net.payrdr.mobile.payment.sdk.SDKException
import net.payrdr.mobile.payment.sdk.SDKForms
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.ui.helper.UIDelegate
import net.payrdr.mobile.payment.sdk.utils.finishWithError
import net.payrdr.mobile.payment.sdk.utils.setupDefaultResult

/**
 * The base class for creating an Activity.
 */
abstract class BaseActivity : AppCompatActivity() {

    private val job = Job()
    protected val workScope: CoroutineScope = CoroutineScope(Dispatchers.IO + job)

    private val uiSetupDelegate: UIDelegate by lazy { UIDelegate(this) }

    /**
     * True for screens that read the SDKForms configuration (card info provider). If it is missing
     * (recreated after process death with nothing to restore) the screen finishes with an error in [onCreate];
     * such screens must return from their onCreate when [isFinishing] after calling super.
     */
    protected open val requiresFormsConfig: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        SDKPayment.restoreConfigIfNeeded(this)
        uiSetupDelegate.onCreate(savedInstanceState)
        uiSetupDelegate.updateConfiguration(this)
        super.onCreate(savedInstanceState)
        setupDefaultResult()
        if (requiresFormsConfig && SDKForms.innerSdkConfig == null) {
            finishWithError(
                SDKException("SDK is not initialized. Please call SDKPayment.init() in Application.onCreate().")
            )
        }
    }

    override fun onResume() {
        super.onResume()
        uiSetupDelegate.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        uiSetupDelegate.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        workScope.cancel()
        super.onDestroy()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(uiSetupDelegate.attachBaseContext(newBase))
    }

    override fun getApplicationContext(): Context {
        return uiSetupDelegate.getApplicationContext(super.getApplicationContext())
    }

    override fun getResources(): Resources {
        return uiSetupDelegate.getResources(super.getResources())
    }

    // https://stackoverflow.com/questions/55265834/change-locale-not-work-after-migrate-to-androidx/58004553#58004553
    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            val uiMode = overrideConfiguration.uiMode
            overrideConfiguration.setTo(baseContext.resources.configuration)
            overrideConfiguration.uiMode = uiMode
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    protected fun supportEdgeToEdgeInsets(
        topInsetView: View,
        botInsetView: View? = null,
    ) {

        val root = findViewById<View>(android.R.id.content)

        root.setOnApplyWindowInsetsListener { view, insets ->
            val topInset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // API 30
                insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()).top
            } else {
                insets.systemWindowInsetTop
            }

            topInsetView.setPadding(
                topInsetView.paddingLeft,
                topInset,
                topInsetView.paddingRight,
                topInsetView.paddingBottom
            )

            botInsetView?.let { v ->
                val botInset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // API 30
                    insets.getInsets(WindowInsets.Type.systemBars()).bottom
                } else {
                    insets.systemWindowInsetBottom
                }
                v.setPadding(
                    botInsetView.paddingLeft,
                    botInsetView.paddingTop,
                    topInsetView.paddingRight,
                    botInset
                )
            }
            insets
        }
    }
}
