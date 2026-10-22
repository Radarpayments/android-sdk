package net.payrdr.mobile.payment.sdk.core.validation.rules

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import org.junit.Test

@SmallTest
class StringRequiredRuleTest {

    private val rule = StringRequiredRule(code = "CODE", message = "MSG")

    @Test
    @Description("non-blank string is valid")
    fun `accepts non-blank string`() {
        rule.validateForError("value").isValid shouldBe true
    }

    @Test
    @Description("empty string fails")
    fun `rejects empty string`() {
        val result = rule.validateForError("")
        result.isValid shouldBe false
        result.errorCode shouldBe "CODE"
        result.errorMessage shouldBe "MSG"
    }

    @Test
    @Description("whitespace-only string fails because isBlank returns true")
    fun `rejects whitespace only`() {
        rule.validateForError("   ").isValid shouldBe false
        rule.validateForError("\t\n").isValid shouldBe false
    }
}
