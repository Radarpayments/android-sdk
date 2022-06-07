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
                setBackground("#63d01c", "#01b27a")
                setHolderName("Mabel Fergie")
                setTextColor("#fff")
                setExpiry("03/20")
                setNumber("42764414")
                setPaymentSystem("visa", true)
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
                setBackground("#444", "#222")
                setHolderName("MARK WATNEY")
                setTextColor("#fff")
                setExpiry("03/25")
                setNumber("55369138")
                setPaymentSystem("mastercard", true)
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
                setBackground("#044b98", "#02356c")
                setHolderName("RICHARD HENDRICKS")
                setTextColor("#fff")
                setExpiry("09/20")
                setNumber("67645463")
                setPaymentSystem("mastercard", true)
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
                setBackground("#ff9f1c", "#fe7a1d")
                setHolderName("JOHN DOE")
                setTextColor("#fff")
                setExpiry("08/22")
                setNumber("53213003")
                setPaymentSystem("mastercard", true)
            }
        }
        allureScreenshot(name = "Qiwi Bank", quality = 1)
    }
}
