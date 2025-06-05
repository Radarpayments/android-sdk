package net.payrdr.mobile.payment.sdk.data

internal object TestCardHelper {

    internal val cardSuccessFull3DS2 = TestCard(
        pan = "5555555555555599",
        expiry = "12/34",
        cvc = "123",
        holder = "CARD HOLDER"
    )

    internal val cardSuccessFrictionless3DS2 = TestCard(
        pan = "4111111111111111",
        expiry = "12/26",
        cvc = "123",
        holder = "CARD HOLDER"
    )

    internal val cardFailFrictionless3DS2 = TestCard(
        pan = "5168494895055780",
        expiry = "12/26",
        cvc = "123",
        holder = "CARD HOLDER"
    )

    internal val cardSuccessAttempt3DS2 = TestCard(
        pan = "4000001111111118",
        expiry = "12/30",
        cvc = "123",
        holder = "CARD HOLDER"
    )

    internal val cardSuccessSSL = TestCard(
        pan = "4444555511113333",
        expiry = "12/26",
        cvc = "123",
        holder = "CARD HOLDER"
    )

    private const val invalidExpiry = "10/35"

    private const val invalidCVC = "000"

    internal const val validVerificationCode = "123456"

    internal const val invalidVerificationCode = "000000"

    internal const val validVerificationCodePaRes = "12345678"

    internal const val invalidVerificationCodePaRes = "00000000"

    fun TestCard.withInvalidCVC(): TestCard {
        return this.copy(cvc = invalidCVC)
    }

    fun TestCard.withInvalidExpiry(): TestCard {
        return this.copy(expiry = invalidExpiry)
    }

    internal fun getLabelForSavedCard(testCard: TestCard): String {
        return "** " + testCard.pan.takeLast(4)
    }

    internal fun getExpireForSavedCard(testCard: TestCard): String {
        return testCard.expiry.takeLast(2) + "/" + testCard.expiry.take(2)
    }

    internal fun getLabelForSavedBindingItem(testCard: TestCard): String {
        return testCard.pan.take(6) + "**" + testCard.pan.takeLast(4) + " " + testCard.expiry
    }

}
