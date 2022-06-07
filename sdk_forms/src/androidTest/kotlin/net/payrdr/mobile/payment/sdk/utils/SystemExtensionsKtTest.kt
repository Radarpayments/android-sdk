package net.payrdr.mobile.payment.sdk.utils

import androidx.test.filters.SmallTest
import io.qameta.allure.android.allureScreenshot
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.form.utils.askToEnableNfc
import net.payrdr.mobile.payment.sdk.test.core.CoreTestActivity
import net.payrdr.mobile.payment.sdk.test.core.CoreUITest
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AllureAndroidJUnit4::class)
class SystemExtensionsKtTest : CoreUITest<CoreTestActivity>(CoreTestActivity::class.java) {

    @Test
    @Description("shouldDisplayNfcEnableDialog")
    fun shouldDisplayNfcEnableDialog() {
        activityTestRule.runOnUiThread {
            askToEnableNfc(activityTestRule.activity)
        }
        allureScreenshot(name = "shouldDisplayNfcEnableDialog_1", quality = 1)
    }
}
