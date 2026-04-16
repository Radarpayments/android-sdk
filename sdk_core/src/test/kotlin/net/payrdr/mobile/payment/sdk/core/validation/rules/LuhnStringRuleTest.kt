package net.payrdr.mobile.payment.sdk.core.validation.rules

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import org.junit.Test

@SmallTest
class LuhnStringRuleTest {

    private val rule = LuhnStringRule(code = "CODE", message = "MSG")

    @Test
    @Description("valid PANs pass Luhn")
    fun `accepts valid PANs`() {
        rule.validateForError("4532896701439077").isValid shouldBe true
        rule.validateForError("5555555555554444").isValid shouldBe true
        rule.validateForError("378282246310005").isValid shouldBe true
    }

    @Test
    @Description("invalid PAN fails with provided code/message")
    fun `rejects invalid PAN`() {
        val result = rule.validateForError("4532896701439078")
        result.isValid shouldBe false
        result.errorCode shouldBe "CODE"
        result.errorMessage shouldBe "MSG"
    }

    @Test
    @Description("empty string passes since sum 0 mod 10 is 0")
    fun `empty string passes`() {
        rule.validateForError("").isValid shouldBe true
    }
}
