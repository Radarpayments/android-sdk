package net.payrdr.mobile.payment.sdk.utils

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import org.junit.Test

@SmallTest
class BasicExtensionsTest {

    // ---- containsAnyOfKeywordIgnoreCase ----

    @Test
    @Description("string matching a status name returns true regardless of case")
    fun `matches status ignoring case`() {
        "DEPOSITED".containsAnyOfKeywordIgnoreCase(OrderStatuses.payedStatues) shouldBe true
        "deposited".containsAnyOfKeywordIgnoreCase(OrderStatuses.payedStatues) shouldBe true
        "Approved".containsAnyOfKeywordIgnoreCase(OrderStatuses.payedStatues) shouldBe true
    }

    @Test
    @Description("string not in the keyword list returns false")
    fun `does not match outside of list`() {
        "CREATED".containsAnyOfKeywordIgnoreCase(OrderStatuses.payedStatues) shouldBe false
        "DECLINED".containsAnyOfKeywordIgnoreCase(OrderStatuses.payedStatues) shouldBe false
    }

    @Test
    @Description("empty string returns false for non-empty list")
    fun `empty string returns false`() {
        "".containsAnyOfKeywordIgnoreCase(OrderStatuses.payedStatues) shouldBe false
    }

    @Test
    @Description("empty keyword list returns false")
    fun `empty keyword list returns false`() {
        "DEPOSITED".containsAnyOfKeywordIgnoreCase(emptyList()) shouldBe false
    }

    @Test
    @Description("substring match does not count — equals with ignoreCase is required")
    fun `partial match returns false`() {
        "DEPOSIT".containsAnyOfKeywordIgnoreCase(OrderStatuses.payedStatues) shouldBe false
        "DEPOSITEDXX".containsAnyOfKeywordIgnoreCase(OrderStatuses.payedStatues) shouldBe false
    }

    // ---- mapToJsonString ----

    @Test
    @Description("empty map produces {}")
    fun `empty map to json`() {
        emptyMap<String, String>().mapToJsonString() shouldBe "{}"
    }

    @Test
    @Description("single entry produces single key/value object")
    fun `single entry to json`() {
        mapOf("a" to "1").mapToJsonString() shouldBe """{"a":"1"}"""
    }

    @Test
    @Description("multiple entries are comma-separated and wrapped")
    fun `multiple entries to json`() {
        val result = mapOf("a" to "1", "b" to "2").mapToJsonString()
        result shouldBe """{"a":"1","b":"2"}"""
    }

    @Test
    @Description("values are emitted verbatim without escaping (documents current behaviour)")
    fun `values are not escaped`() {
        mapOf("k" to "v\"x").mapToJsonString() shouldBe """{"k":"v"x"}"""
    }
}
