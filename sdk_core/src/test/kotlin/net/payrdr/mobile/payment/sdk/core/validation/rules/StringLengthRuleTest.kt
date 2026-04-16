package net.payrdr.mobile.payment.sdk.core.validation.rules

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import org.junit.Test

@SmallTest
class StringLengthRuleTest {

    @Test
    @Description("length inside inclusive bounds passes")
    fun `accepts string within bounds`() {
        val rule = StringLengthRule(code = "C", message = "M", minLength = 3, maxLength = 5)
        rule.validateForError("abc").isValid shouldBe true
        rule.validateForError("abcde").isValid shouldBe true
    }

    @Test
    @Description("length below min fails with code/message")
    fun `rejects too short`() {
        val rule = StringLengthRule(code = "C", message = "M", minLength = 3, maxLength = 5)
        val result = rule.validateForError("ab")
        result.isValid shouldBe false
        result.errorCode shouldBe "C"
        result.errorMessage shouldBe "M"
    }

    @Test
    @Description("length above max fails")
    fun `rejects too long`() {
        val rule = StringLengthRule(code = "C", message = "M", minLength = 3, maxLength = 5)
        rule.validateForError("abcdef").isValid shouldBe false
    }

    @Test
    @Description("defaults allow any non-negative length")
    fun `defaults accept any string`() {
        val rule = StringLengthRule(code = "C", message = "M")
        rule.validateForError("").isValid shouldBe true
        rule.validateForError("anything").isValid shouldBe true
    }
}
