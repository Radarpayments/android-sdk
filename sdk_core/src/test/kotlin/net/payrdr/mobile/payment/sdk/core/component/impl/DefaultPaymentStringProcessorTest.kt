package net.payrdr.mobile.payment.sdk.core.component.impl

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.core.component.PaymentStringProcessor
import net.payrdr.mobile.payment.sdk.core.model.CardBindingIdIdentifier
import net.payrdr.mobile.payment.sdk.core.model.CardInfo
import net.payrdr.mobile.payment.sdk.core.model.CardPanIdentifier
import net.payrdr.mobile.payment.sdk.core.model.ExpiryDate
import net.payrdr.mobile.payment.sdk.core.model.MSDKRegisteredFrom
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

@SmallTest
@Suppress("MaxLineLength")
class DefaultPaymentStringProcessorTest {

    private lateinit var paymentStringProcessor: PaymentStringProcessor

    @Before
    fun setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"))
        paymentStringProcessor = DefaultPaymentStringProcessor()
    }

    @Test
    @Description("should return filled template for a new card")
    fun `should return filled template for a new card`() {
        val template = paymentStringProcessor.createPaymentString(
            order = "7f472085-399e-414e-b51c-a7b538aee497",
            timestamp = 1594009580806L,
            uuid = "fd4b1011-727a-41e8-95b4-d7092d729022",
            cardInfo = CardInfo(
                identifier = CardPanIdentifier("4532896701439077"),
                cvv = "444",
                expDate = ExpiryDate(2020, 12),
                cardHolder = "CardHolderName",
            ),
            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
        )

        template shouldBe "2020-07-06T07:26:20+03:00/fd4b1011-727a-41e8-95b4-d7092d729022/4532896701439077/444/202012/7f472085-399e-414e-b51c-a7b538aee497//CardHolderName/MSDK_CORE"
    }

    @Test
    @Description("should return filled template for a new card without cvv")
    fun `should return filled template for a new card without cvv`() {
        val template = paymentStringProcessor.createPaymentString(
            order = "7f472085-399e-414e-b51c-a7b538aee497",
            timestamp = 1594009580806L,
            uuid = "fd4b1011-727a-41e8-95b4-d7092d729022",
            cardInfo = CardInfo(
                identifier = CardPanIdentifier("4532896701439077"),
                expDate = ExpiryDate(2020, 12),
                cardHolder = "CardHolderName",
            ),
            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
        )

        template shouldBe "2020-07-06T07:26:20+03:00/fd4b1011-727a-41e8-95b4-d7092d729022/4532896701439077//202012/7f472085-399e-414e-b51c-a7b538aee497//CardHolderName/MSDK_CORE"
    }

    @Test
    @Description("should return filled template for a new card without exp date")
    fun `should return filled template for a new card without exp date`() {
        val template = paymentStringProcessor.createPaymentString(
            order = "7f472085-399e-414e-b51c-a7b538aee497",
            timestamp = 1594009580806L,
            uuid = "fd4b1011-727a-41e8-95b4-d7092d729022",
            cardInfo = CardInfo(
                identifier = CardPanIdentifier("4532896701439077"),
                cvv = "444",
                cardHolder = "CardHolderName",
            ),
            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
        )

        template shouldBe "2020-07-06T07:26:20+03:00/fd4b1011-727a-41e8-95b4-d7092d729022/4532896701439077/444//7f472085-399e-414e-b51c-a7b538aee497//CardHolderName/MSDK_CORE"
    }

    @Test
    @Description("should return filled template for a new card without cvv and exp date")
    fun `should return filled template for a new card without cvv and exp date`() {
        val template = paymentStringProcessor.createPaymentString(
            order = "7f472085-399e-414e-b51c-a7b538aee497",
            timestamp = 1594009580806L,
            uuid = "fd4b1011-727a-41e8-95b4-d7092d729022",
            cardInfo = CardInfo(
                identifier = CardPanIdentifier("4532896701439077"),
                cardHolder = "CardHolderName",
            ),
            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
        )
        template shouldBe "2020-07-06T07:26:20+03:00/fd4b1011-727a-41e8-95b4-d7092d729022/4532896701439077///7f472085-399e-414e-b51c-a7b538aee497//CardHolderName/MSDK_CORE"
    }

    @Test
    @Description("should return filled template for saved card")
    fun `should return filled template for saved card`() {
        val template = paymentStringProcessor.createPaymentString(
            order = "7f472085-399e-414e-b51c-a7b538aee497",
            timestamp = 1594009580806L,
            uuid = "fd4b1011-727a-41e8-95b4-d7092d729022",
            cardInfo = CardInfo(
                identifier = CardBindingIdIdentifier("47eb0336-5ad9-4e03-8a1e-b9f3656ec768"),
                cvv = "444",
                expDate = ExpiryDate(2020, 12)
            ),
            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
        )

        template shouldBe "2020-07-06T07:26:20+03:00/fd4b1011-727a-41e8-95b4-d7092d729022//444/202012/7f472085-399e-414e-b51c-a7b538aee497/47eb0336-5ad9-4e03-8a1e-b9f3656ec768//MSDK_CORE"
    }

    @Test
    @Description("should return filled template for saved card without cvv")
    fun `should return filled template for saved card without cvv`() {
        val template = paymentStringProcessor.createPaymentString(
            order = "7f472085-399e-414e-b51c-a7b538aee497",
            timestamp = 1594009580806L,
            uuid = "fd4b1011-727a-41e8-95b4-d7092d729022",
            cardInfo = CardInfo(
                identifier = CardBindingIdIdentifier("47eb0336-5ad9-4e03-8a1e-b9f3656ec768"),
                expDate = ExpiryDate(2020, 12),
            ),
            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
        )

        template shouldBe "2020-07-06T07:26:20+03:00/fd4b1011-727a-41e8-95b4-d7092d729022///202012/7f472085-399e-414e-b51c-a7b538aee497/47eb0336-5ad9-4e03-8a1e-b9f3656ec768//MSDK_CORE"
    }

    @Test
    @Description("should return filled template for saved card without exp date")
    fun `should return filled template for saved card without exp date`() {
        val template = paymentStringProcessor.createPaymentString(
            order = "7f472085-399e-414e-b51c-a7b538aee497",
            timestamp = 1594009580806L,
            uuid = "fd4b1011-727a-41e8-95b4-d7092d729022",
            cardInfo = CardInfo(
                identifier = CardBindingIdIdentifier("47eb0336-5ad9-4e03-8a1e-b9f3656ec768"),
                cvv = "444"
            ),
            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
        )

        template shouldBe "2020-07-06T07:26:20+03:00/fd4b1011-727a-41e8-95b4-d7092d729022//444//7f472085-399e-414e-b51c-a7b538aee497/47eb0336-5ad9-4e03-8a1e-b9f3656ec768//MSDK_CORE"
    }

    @Test
    @Description("should return filled template for saved card without cvv and exp date")
    fun `should return filled template for saved card without cvv and exp date`() {
        val template = paymentStringProcessor.createPaymentString(
            order = "7f472085-399e-414e-b51c-a7b538aee497",
            timestamp = 1594009580806L,
            uuid = "fd4b1011-727a-41e8-95b4-d7092d729022",
            cardInfo = CardInfo(
                identifier = CardBindingIdIdentifier("47eb0336-5ad9-4e03-8a1e-b9f3656ec768")
            ),
            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
        )

        template shouldBe "2020-07-06T07:26:20+03:00/fd4b1011-727a-41e8-95b4-d7092d729022////7f472085-399e-414e-b51c-a7b538aee497/47eb0336-5ad9-4e03-8a1e-b9f3656ec768//MSDK_CORE"
    }

    @Test
    @Description("should return filled template for a new card without cvv")
    fun `should return filled template for a new card without mdOrder`() {
        val template = paymentStringProcessor.createPaymentString(
            timestamp = 1594009580806L,
            uuid = "fd4b1011-727a-41e8-95b4-d7092d729022",
            cardInfo = CardInfo(
                identifier = CardPanIdentifier("4532896701439077"),
                cvv = "444",
                expDate = ExpiryDate(2020, 12),
                cardHolder = "CardHolderName",
            ),
            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
        )

        template shouldBe "2020-07-06T07:26:20+03:00/fd4b1011-727a-41e8-95b4-d7092d729022/4532896701439077/444/202012///CardHolderName/MSDK_CORE"
    }

    @Test
    @Description("should return filled template for a new card without cvv")
    fun `should return filled template for a new card without mdOrder cvv`() {
        val template = paymentStringProcessor.createPaymentString(
            timestamp = 1594009580806L,
            uuid = "fd4b1011-727a-41e8-95b4-d7092d729022",
            cardInfo = CardInfo(
                identifier = CardPanIdentifier("4532896701439077"),
                expDate = ExpiryDate(2020, 12),
                cardHolder = "CardHolderName",
            ),
            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
        )

        template shouldBe "2020-07-06T07:26:20+03:00/fd4b1011-727a-41e8-95b4-d7092d729022/4532896701439077//202012///CardHolderName/MSDK_CORE"
    }

    @Test
    @Description("should return filled template for a new card without mdOrder exp date")
    fun `should return filled template for a new card without mdOrder exp date`() {
        val template = paymentStringProcessor.createPaymentString(
            timestamp = 1594009580806L,
            uuid = "fd4b1011-727a-41e8-95b4-d7092d729022",
            cardInfo = CardInfo(
                identifier = CardPanIdentifier("4532896701439077"),
                cvv = "444",
                cardHolder = "CardHolderName",
            ),
            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
        )

        template shouldBe "2020-07-06T07:26:20+03:00/fd4b1011-727a-41e8-95b4-d7092d729022/4532896701439077/444////CardHolderName/MSDK_CORE"
    }

    @Test
    @Description("should return filled template for a new card without mdOrder cvv and exp date")
    fun `should return filled template for a new card without mdOrder cvv and exp date`() {
        val template = paymentStringProcessor.createPaymentString(
            timestamp = 1594009580806L,
            uuid = "fd4b1011-727a-41e8-95b4-d7092d729022",
            cardInfo = CardInfo(
                identifier = CardPanIdentifier("4532896701439077"),
                cardHolder = "CardHolderName",
            ),
            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
        )
        template shouldBe "2020-07-06T07:26:20+03:00/fd4b1011-727a-41e8-95b4-d7092d729022/4532896701439077/////CardHolderName/MSDK_CORE"
    }

    @Test
    @Description("should return filled template for saved card without mdOrder")
    fun `should return filled template for saved card without mdOrder`() {
        val template = paymentStringProcessor.createPaymentString(
            timestamp = 1594009580806L,
            uuid = "fd4b1011-727a-41e8-95b4-d7092d729022",
            cardInfo = CardInfo(
                identifier = CardBindingIdIdentifier("47eb0336-5ad9-4e03-8a1e-b9f3656ec768"),
                expDate = ExpiryDate(2020, 12),
                cvv = "444",
                cardHolder = "CardHolderName",
            ),
            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
        )

        template shouldBe "2020-07-06T07:26:20+03:00/fd4b1011-727a-41e8-95b4-d7092d729022//444/202012//47eb0336-5ad9-4e03-8a1e-b9f3656ec768/CardHolderName/MSDK_CORE"
    }

    @Test
    @Description("should return filled template for saved card without mdOrder cvv")
    fun `should return filled template for saved card without mdOrder cvv`() {
        val template = paymentStringProcessor.createPaymentString(
            timestamp = 1594009580806L,
            uuid = "fd4b1011-727a-41e8-95b4-d7092d729022",
            cardInfo = CardInfo(
                identifier = CardBindingIdIdentifier("47eb0336-5ad9-4e03-8a1e-b9f3656ec768"),
                expDate = ExpiryDate(2020, 12),
                cardHolder = "CardHolderName",
            ),
            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
        )

        template shouldBe "2020-07-06T07:26:20+03:00/fd4b1011-727a-41e8-95b4-d7092d729022///202012//47eb0336-5ad9-4e03-8a1e-b9f3656ec768/CardHolderName/MSDK_CORE"
    }

    @Test
    @Description("should return filled template for saved card without mdOrder exp date")
    fun `should return filled template for saved card without mdOrder exp date`() {
        val template = paymentStringProcessor.createPaymentString(
            timestamp = 1594009580806L,
            uuid = "fd4b1011-727a-41e8-95b4-d7092d729022",
            cardInfo = CardInfo(
                identifier = CardBindingIdIdentifier("47eb0336-5ad9-4e03-8a1e-b9f3656ec768"),
                cvv = "444",
                cardHolder = "CardHolderName",
            ),
            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
        )

        template shouldBe "2020-07-06T07:26:20+03:00/fd4b1011-727a-41e8-95b4-d7092d729022//444///47eb0336-5ad9-4e03-8a1e-b9f3656ec768/CardHolderName/MSDK_CORE"
    }

    @Test
    @Description("should return filled template for saved card without mdOrder cvv and exp date")
    fun `should return filled template for saved card without mdOrder cvv and exp date`() {
        val template = paymentStringProcessor.createPaymentString(
            timestamp = 1594009580806L,
            uuid = "fd4b1011-727a-41e8-95b4-d7092d729022",
            cardInfo = CardInfo(
                identifier = CardBindingIdIdentifier("47eb0336-5ad9-4e03-8a1e-b9f3656ec768"),
                cardHolder = "CardHolderName",
            ),
            registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
        )

        template shouldBe "2020-07-06T07:26:20+03:00/fd4b1011-727a-41e8-95b4-d7092d729022/////47eb0336-5ad9-4e03-8a1e-b9f3656ec768/CardHolderName/MSDK_CORE"
    }
}
