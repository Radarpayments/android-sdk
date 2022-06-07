package net.payrdr.mobile.payment.sdk

import androidx.test.filters.SmallTest
import com.google.android.gms.wallet.PaymentDataRequest
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.comparables.shouldNotBeEqualComparingTo
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.beEmpty
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.form.GooglePayConfigBuilder
import net.payrdr.mobile.payment.sdk.form.model.Theme
import net.payrdr.mobile.payment.sdk.form.ui.helper.Locales.english
import net.payrdr.mobile.payment.sdk.form.ui.helper.Locales.french
import net.payrdr.mobile.payment.sdk.form.ui.helper.Locales.german
import net.payrdr.mobile.payment.sdk.form.ui.helper.Locales.russian
import net.payrdr.mobile.payment.sdk.form.ui.helper.Locales.spanish
import net.payrdr.mobile.payment.sdk.form.ui.helper.Locales.ukrainian
import org.junit.Test

@SmallTest
class GooglePayConfigBuilderTest {

    @Test
    @Description("should return theme")
    fun `should return theme`() {
        GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        ).theme(Theme.DARK)
            .build()
            .theme shouldBe Theme.DARK

        GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        ).theme(Theme.LIGHT)
            .build()
            .theme shouldBe Theme.LIGHT

        GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        ).theme(Theme.SYSTEM)
            .build()
            .theme shouldBe Theme.SYSTEM

        GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        ).theme(Theme.DEFAULT)
            .build()
            .theme shouldBe Theme.DEFAULT
    }

    @Test
    @Description("should return locale")
    fun `should return locale`() {
        GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        ).locale(english())
            .build()
            .locale.language shouldBe "en"

        GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        ).locale(german())
            .build()
            .locale.language shouldBe "de"

        GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        ).locale(french())
            .build()
            .locale.language shouldBe "fr"

        GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        ).locale(spanish())
            .build()
            .locale.language shouldBe "es"

        GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        ).locale(russian())
            .build()
            .locale.language shouldBe "ru"

        GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        ).locale(ukrainian())
            .build()
            .locale.language shouldBe "uk"
    }

    @Test
    @Description("should return generated uuid value by default")
    fun `should return generated uuid value by default`() {
        val order = "2545f609-490b-4c8d-a3fb-be1f8d30fa77"

        val firstUUID = GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        ).build().uuid

        val secondUUID = GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        ).build().uuid

        firstUUID shouldNot beEmpty()
        secondUUID shouldNot beEmpty()
        firstUUID shouldNotBeEqualComparingTo secondUUID
    }

    @Test
    @Description("should return defined uuid")
    fun `should return defined uuid`() {
        val expectedUuid = "62d0bb9e-111b-4c28-a79e-e7fb8d1791eb"

        val actualUuid = GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        ).uuid(expectedUuid)
            .build()
            .uuid

        actualUuid shouldBe expectedUuid
    }

    @Test
    @Description("should return same generated value of uuid for the same builder")
    fun `should return same generated value of uuid for the same builder`() {
        val builder = GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        )

        val firstUUID = builder
            .build()
            .uuid
        val secondUUID = builder
            .build()
            .uuid

        firstUUID shouldNot beEmpty()
        secondUUID shouldNot beEmpty()
        firstUUID shouldBe secondUUID
    }

    @Test
    @Description("should return current timestamp by default")
    fun `should return current timestamp by default`() {
        val order = "039e5501-d39b-47c1-a0e5-bd1d3ae917ad"

        val beforeExecute = System.currentTimeMillis()
        Thread.sleep(100)
        val firstTimestamp = GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        ).build().timestamp
        Thread.sleep(100)
        val secondTimestamp = GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        ).build().timestamp
        Thread.sleep(100)
        val afterExecute = System.currentTimeMillis()

        firstTimestamp shouldNotBe 0L
        secondTimestamp shouldNotBe 0L
        firstTimestamp shouldNotBeEqualComparingTo secondTimestamp
        firstTimestamp shouldBeGreaterThan beforeExecute
        secondTimestamp shouldBeGreaterThan beforeExecute
        firstTimestamp shouldBeLessThan afterExecute
        secondTimestamp shouldBeLessThan afterExecute
    }

    @Test
    @Description("should return defined timestamp")
    fun `should return defined timestamp`() {
        val expectedTimestamp = 1593791597L

        val actualTimestamp = GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        ).timestamp(expectedTimestamp)
            .build()
            .timestamp

        actualTimestamp shouldBe expectedTimestamp
    }

    @Test
    @Description("should return same value of timestamp for the same builder")
    fun `should return same value of timestamp for the same builder`() {
        val builder = GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        )

        val firstTimestamp = builder
            .build()
            .timestamp
        val secondTimestamp = builder
            .build()
            .timestamp

        firstTimestamp shouldNotBe 0L
        secondTimestamp shouldNotBe 0L
        firstTimestamp shouldBe secondTimestamp
    }

    @Test
    @Description("should return testEnvironment")
    fun `should return testEnvironment`() {
        GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        )
            .testEnvironment(true)
            .build()
            .testEnvironment shouldBe true

        GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        )
            .testEnvironment(false)
            .build()
            .testEnvironment shouldBe false
    }

    @Test
    @Description("should return testEnvironment false by default")
    fun `should return testEnvironment false by default`() {
        GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = PaymentDataRequest.fromJson("{}")
        ).build()
            .testEnvironment shouldBe false
    }

    @Test
    @Description("should return paymentData")
    fun `should return paymentData`() {
        val actualPaymentData = PaymentDataRequest.fromJson("{}")
        GooglePayConfigBuilder(
            order = "394f2c04-430c-4102-81e6-451d79234fc8",
            paymentData = actualPaymentData
        ).build().paymentData shouldBe actualPaymentData
    }

    @Test
    @Description("should return theme without order")
    fun `should return theme without order`() {
        GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        ).theme(Theme.DARK)
            .build()
            .theme shouldBe Theme.DARK

        GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        ).theme(Theme.LIGHT)
            .build()
            .theme shouldBe Theme.LIGHT

        GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        ).theme(Theme.SYSTEM)
            .build()
            .theme shouldBe Theme.SYSTEM

        GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        ).theme(Theme.DEFAULT)
            .build()
            .theme shouldBe Theme.DEFAULT
    }

    @Test
    @Description("should return locale without order")
    fun `should return locale without order`() {
        GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        ).locale(english())
            .build()
            .locale.language shouldBe "en"

        GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        ).locale(german())
            .build()
            .locale.language shouldBe "de"

        GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        ).locale(french())
            .build()
            .locale.language shouldBe "fr"

        GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        ).locale(spanish())
            .build()
            .locale.language shouldBe "es"

        GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        ).locale(russian())
            .build()
            .locale.language shouldBe "ru"

        GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        ).locale(ukrainian())
            .build()
            .locale.language shouldBe "uk"
    }

    @Test
    @Description("should return generated uuid value by default without order")
    fun `should return generated uuid value by default without order`() {

        val firstUUID = GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        ).build().uuid

        val secondUUID = GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        ).build().uuid

        firstUUID shouldNot beEmpty()
        secondUUID shouldNot beEmpty()
        firstUUID shouldNotBeEqualComparingTo secondUUID
    }

    @Test
    @Description("should return defined uuid without order")
    fun `should return defined uuid without order`() {
        val expectedUuid = "62d0bb9e-111b-4c28-a79e-e7fb8d1791eb"

        val actualUuid = GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        ).uuid(expectedUuid)
            .build()
            .uuid

        actualUuid shouldBe expectedUuid
    }

    @Test
    @Description("should return same generated value of uuid for the same builder without order")
    fun `should return same generated value of uuid for the same builder without order`() {
        val builder = GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        )

        val firstUUID = builder
            .build()
            .uuid
        val secondUUID = builder
            .build()
            .uuid

        firstUUID shouldNot beEmpty()
        secondUUID shouldNot beEmpty()
        firstUUID shouldBe secondUUID
    }

    @Test
    @Description("should return current timestamp by default without order")
    fun `should return current timestamp by default without order`() {
        val beforeExecute = System.currentTimeMillis()
        Thread.sleep(100)
        val firstTimestamp = GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        ).build().timestamp
        Thread.sleep(100)
        val secondTimestamp = GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        ).build().timestamp
        Thread.sleep(100)
        val afterExecute = System.currentTimeMillis()

        firstTimestamp shouldNotBe 0L
        secondTimestamp shouldNotBe 0L
        firstTimestamp shouldNotBeEqualComparingTo secondTimestamp
        firstTimestamp shouldBeGreaterThan beforeExecute
        secondTimestamp shouldBeGreaterThan beforeExecute
        firstTimestamp shouldBeLessThan afterExecute
        secondTimestamp shouldBeLessThan afterExecute
    }

    @Test
    @Description("should return defined timestamp without order")
    fun `should return defined timestamp without order`() {
        val expectedTimestamp = 1593791597L

        val actualTimestamp = GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        ).timestamp(expectedTimestamp)
            .build()
            .timestamp

        actualTimestamp shouldBe expectedTimestamp
    }

    @Test
    @Description("should return same value of timestamp for the same builder without order")
    fun `should return same value of timestamp for the same builder without order`() {
        val builder = GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        )

        val firstTimestamp = builder
            .build()
            .timestamp
        val secondTimestamp = builder
            .build()
            .timestamp

        firstTimestamp shouldNotBe 0L
        secondTimestamp shouldNotBe 0L
        firstTimestamp shouldBe secondTimestamp
    }

    @Test
    @Description("should return testEnvironment without order")
    fun `should return testEnvironment without order`() {
        GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        )
            .testEnvironment(true)
            .build()
            .testEnvironment shouldBe true

        GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        )
            .testEnvironment(false)
            .build()
            .testEnvironment shouldBe false
    }

    @Test
    @Description("should return testEnvironment false by default without order")
    fun `should return testEnvironment false by default without order`() {
        GooglePayConfigBuilder(
            paymentData = PaymentDataRequest.fromJson("{}")
        ).build()
            .testEnvironment shouldBe false
    }

    @Test
    @Description("should return paymentData without order")
    fun `should return paymentData without order`() {
        val actualPaymentData = PaymentDataRequest.fromJson("{}")
        GooglePayConfigBuilder(
            paymentData = actualPaymentData
        ).build().paymentData shouldBe actualPaymentData
    }
}
