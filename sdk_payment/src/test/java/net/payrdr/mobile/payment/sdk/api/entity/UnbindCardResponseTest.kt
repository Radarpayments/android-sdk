package net.payrdr.mobile.payment.sdk.api.entity

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import org.json.JSONObject
import org.junit.Test

@SmallTest
class UnbindCardResponseTest {

    @Test
    fun shouldReturnTrueWhenErrorCodeIsZero() {
        val json = JSONObject("""{"errorCode":0}""")

        val result = UnbindCardResponse.fromJson(json)

        result.isSuccess() shouldBe true
    }

    @Test
    fun shouldReturnFalseWhenErrorCodeIsNonZero() {
        val json = JSONObject("""{"errorCode":5}""")

        val result = UnbindCardResponse.fromJson(json)

        result.isSuccess() shouldBe false
    }

    @Test
    fun shouldReturnFalseWhenErrorCodeIsNegative() {
        val json = JSONObject("""{"errorCode":-1}""")

        val result = UnbindCardResponse.fromJson(json)

        result.isSuccess() shouldBe false
    }
}
