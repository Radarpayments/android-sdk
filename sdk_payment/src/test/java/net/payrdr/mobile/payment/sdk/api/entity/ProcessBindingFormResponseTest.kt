package net.payrdr.mobile.payment.sdk.api.entity

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import org.json.JSONObject
import org.junit.Test

@SmallTest
class ProcessBindingFormResponseTest {

    @Test
    fun shouldParseZeroErrorCode() {
        val json = JSONObject("""{"errorCode":0}""")

        val result = ProcessBindingFormResponse.fromJson(json)

        result.errorCode shouldBe 0
    }

    @Test
    fun shouldParseNonZeroErrorCode() {
        val json = JSONObject("""{"errorCode":2}""")

        val result = ProcessBindingFormResponse.fromJson(json)

        result.errorCode shouldBe 2
    }
}
