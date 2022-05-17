package net.payrdr.mobile.payment.sample.kotlin.payment

import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class PaymentFormActivityTest {
    private val server: MockWebServer = MockWebServer()

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun shouldReturnData() {

    }

}
