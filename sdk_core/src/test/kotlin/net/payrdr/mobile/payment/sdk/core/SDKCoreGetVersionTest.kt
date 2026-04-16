package net.payrdr.mobile.payment.sdk.core

import android.content.Context
import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.qameta.allure.kotlin.Description
import org.junit.Test

@SmallTest
class SDKCoreGetVersionTest {

    @Test
    @Description("getSDKVersion returns the BuildConfig SDK_CORE_VERSION_NUMBER value")
    fun `returns BuildConfig version`() {
        val context: Context = mockk(relaxed = true)
        val sdkCore = SDKCore(context)

        sdkCore.getSDKVersion() shouldBe BuildConfig.SDK_CORE_VERSION_NUMBER
    }
}
