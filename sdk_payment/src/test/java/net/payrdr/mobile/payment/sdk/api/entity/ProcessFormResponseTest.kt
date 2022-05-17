package net.payrdr.mobile.payment.sdk.api.entity

import androidx.test.filters.SmallTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

@SmallTest
class ProcessFormResponseTest {

    @Test
    fun shouldReturnProcessFormObject() {
        val str = """
            {
                    "redirect":"../merchants/rbs/finish.html?orderId=59881695-6fe4-747b-9b82-52a31917ef58&lang=ru",
                    "info":"Your payment has been processed and is being redirected...",
                    "errorCode":0,
                    "is3DSVer2":false
            }
        """.trimIndent()

        val res = ProcessFormResponse.fromJson(jsonObject = JSONObject(str))

        assertEquals(0, res.errorCode)
        assertEquals(false, res.is3DSVer2)
    }
}
