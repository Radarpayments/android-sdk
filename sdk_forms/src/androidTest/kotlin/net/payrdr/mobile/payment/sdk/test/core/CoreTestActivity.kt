package net.payrdr.mobile.payment.sdk.test.core

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import net.payrdr.mobile.payment.sdk.form.test.R
import net.payrdr.mobile.payment.sdk.form.ui.BaseActivity

/**
 * Activity used to test UI components such as View, Dialog, Fragment and others.
 */
class CoreTestActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
    }

    /**
     * Sets the passed [view] to the screen layout.
     *
     * @param view View for testing.
     */
    fun setTestView(view: View) {
        findViewById<ViewGroup>(R.id.testContainer).apply {
            removeAllViews()
            addView(view)
        }
    }
}
