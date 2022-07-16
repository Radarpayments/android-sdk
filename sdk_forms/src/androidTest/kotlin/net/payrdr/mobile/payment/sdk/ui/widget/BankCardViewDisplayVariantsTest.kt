package net.payrdr.mobile.payment.sdk.ui.widget

import android.content.Context
import androidx.test.filters.SmallTest
import io.qameta.allure.android.allureScreenshot
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.form.ui.widget.BankCardView
import net.payrdr.mobile.payment.sdk.test.core.CoreUIViewTest
import net.payrdr.mobile.payment.sdk.test.junit.ConfigurationLocales
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

@SmallTest
class BankCardViewDisplayVariantsTest : CoreUIViewTest<BankCardView>() {

    private lateinit var server: MockWebServer

    override fun prepareView(context: Context): BankCardView {
        return BankCardView(context)
    }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun MockWebServer.enqueueFile(fileName: String): String {
        val fileContent = this::class.java.getResource("/$fileName")!!.readText()
        server.enqueue(
            MockResponse().setBody(fileContent)
        )
        return server.url("/").toString()
    }

    @Suppress("LongMethod", "ComplexMethod")
    @ConfigurationLocales(["en"])
    @Test
    @Description("shouldCardDisplayVariantsLocalBank")
    fun shouldCardDisplayVariantsLocalBank() {
        allureScreenshot(name = "Empty", quality = 1)
        val url = server.enqueueFile("bank-invert.svg")
        activityTestRule.runOnUiThread {
            testedView.apply {
                setBankLogoUrl(url)
            }
        }

        allureScreenshot(name = "Bank", quality = 1)
    }

    @Suppress("LongMethod", "ComplexMethod")
    @ConfigurationLocales(["en"])
    @Test
    @Description("shouldCardDisplayVariantsLocalTinkoff")
    fun shouldCardDisplayVariantsLocalTinkoff() {
        allureScreenshot(name = "Empty", quality = 1)
        val url = server.enqueueFile("tinkoff-invert.svg")
        activityTestRule.runOnUiThread {
            testedView.apply {
                setBankLogoUrl(url)
            }
        }
        allureScreenshot(name = "Tinkoff Bank", quality = 1)
    }

    @Suppress("LongMethod", "ComplexMethod")
    @ConfigurationLocales(["en"])
    @Test
    @Description("shouldCardDisplayVariantsLocalGazprom")
    fun shouldCardDisplayVariantsLocalGazprom() {
        allureScreenshot(name = "Empty", quality = 1)
        val url = server.enqueueFile("gazprom-invert.svg")
        activityTestRule.runOnUiThread {
            testedView.apply {
                setBankLogoUrl(url)
            }
        }
        allureScreenshot(name = "Gazprom Bank", quality = 1)
    }

    @Suppress("LongMethod", "ComplexMethod")
    @ConfigurationLocales(["en"])
    @Test
    @Description("shouldCardDisplayVariantsLocalQiwi")
    fun shouldCardDisplayVariantsLocalQiwi() {
        allureScreenshot(name = "Empty", quality = 1)
        val url = server.enqueueFile("qiwi-invert.svg")
        activityTestRule.runOnUiThread {
            testedView.apply {
                setBankLogoUrl(url)
            }
        }
        allureScreenshot(name = "Qiwi Bank", quality = 1)
    }
}
