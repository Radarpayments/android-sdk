package net.payrdr.mobile.payment.sdk.utils

import androidx.test.filters.SmallTest
import net.payrdr.mobile.payment.sdk.form.utils.askToEnableNfc
import net.payrdr.mobile.payment.sdk.test.core.CoreTestActivity
import net.payrdr.mobile.payment.sdk.test.core.CoreUITest
import org.junit.Test

@SmallTest
class SystemExtensionsKtTest : CoreUITest<CoreTestActivity>(CoreTestActivity::class.java) {

    @Test
    fun shouldDisplayNfcEnableDialog() {
        activityTestRule.runOnUiThread {
            askToEnableNfc(activityTestRule.activity)
        }
        takeScreen()
    }
}
