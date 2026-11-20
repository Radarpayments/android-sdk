package net.payrdr.mobile.payment.sdk.model

import android.os.Parcel
import androidx.test.filters.SmallTest
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AllureAndroidJUnit4::class)
class PaymentInfoGooglePayTest {

    @Test
    @Description("shouldKeepPayerDataAfterParcel")
    fun shouldKeepPayerDataAfterParcel() {
        val info = PaymentInfoGooglePay(
            order = "order-1",
            paymentToken = "token",
            email = "payer@example.com",
            phone = "+357 99 123456"
        )

        assertEquals(info, info.parcelRoundTrip())
    }

    @Test
    @Description("shouldKeepMissingPayerDataAfterParcel")
    fun shouldKeepMissingPayerDataAfterParcel() {
        val info = PaymentInfoGooglePay(order = "order-1", paymentToken = "token")

        assertEquals(info, info.parcelRoundTrip())
    }

    private fun PaymentInfoGooglePay.parcelRoundTrip(): PaymentInfoGooglePay {
        val parcel = Parcel.obtain()
        return try {
            writeToParcel(parcel, 0)
            parcel.setDataPosition(0)
            PaymentInfoGooglePay.CREATOR.createFromParcel(parcel)
        } finally {
            parcel.recycle()
        }
    }
}
