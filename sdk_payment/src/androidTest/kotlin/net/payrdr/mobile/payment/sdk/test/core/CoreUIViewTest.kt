package net.payrdr.mobile.payment.sdk.test.core

import android.content.Context
import android.view.View
import org.junit.Before

/**
 * Base class for creating tests for View. Starts an Activity with an object
 * on it class View intended for testing.
 */
abstract class CoreUIViewTest<VIEW : View> :
    CoreUITest<CoreTestActivity>(CoreTestActivity::class.java) {

    protected lateinit var testedView: VIEW
        private set

    private lateinit var wrapperView: View

    @Before
    fun setup() {
        val context = getActivity().window.decorView.rootView.context
        testedView = prepareView(context)
        getActivity().runOnUiThread {
            wrapperView = wrapView(context, testedView)
            getActivity().setTestView(wrapperView)
        }
    }

    /**
     * Should return a [VIEW] to be positioned on the Activity's screen.
     *
     * @param context activity context.
     * @return View for testing.
     */
    abstract fun prepareView(context: Context): VIEW

    /**
     * Override if necessary to wrap the test View.
     *
     * @param context activity context.
     * @param view the View under test.
     * @return View containing the View being tested, or the View itself, if a wrapper is not required.
     */
    open fun wrapView(context: Context, view: VIEW): View = view
}
