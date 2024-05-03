package net.payrdr.mobile.payment.sdk.data

class TestClientIdHelper(startClientId: Long) {

    private var clientIdShift = startClientId

    fun getNewTestClientId(): String {
        return (++clientIdShift).toString()
    }
}
