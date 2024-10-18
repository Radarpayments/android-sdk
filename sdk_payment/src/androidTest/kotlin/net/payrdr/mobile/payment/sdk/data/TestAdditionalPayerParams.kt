package net.payrdr.mobile.payment.sdk.data

data class TestAdditionalPayerParams(
    val billingPayerData: Map<String, String>,
    val email: String?,
    val mobilePhone: String?,
) {
    companion object {
        val DEFAULT = TestAdditionalPayerParams(
            billingPayerData = emptyMap(),
            email = null,
            mobilePhone = null
        )
    }
}
