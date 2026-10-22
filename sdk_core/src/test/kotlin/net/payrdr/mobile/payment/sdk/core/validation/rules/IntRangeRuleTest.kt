package net.payrdr.mobile.payment.sdk.core.validation.rules

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import org.junit.Test

@SmallTest
class IntRangeRuleTest {

    @Test
    @Description("value inside inclusive range is valid")
    fun `accepts value inside range`() {
        val rule = IntRangeRule(code = "C", message = "M", min = 1, max = 10)
        rule.validateForError(1).isValid shouldBe true
        rule.validateForError(5).isValid shouldBe true
        rule.validateForError(10).isValid shouldBe true
    }

    @Test
    @Description("value below min fails with provided code/message")
    fun `rejects value below min`() {
        val rule = IntRangeRule(code = "C", message = "M", min = 1, max = 10)
        val result = rule.validateForError(0)
        result.isValid shouldBe false
        result.errorCode shouldBe "C"
        result.errorMessage shouldBe "M"
    }

    @Test
    @Description("value above max fails")
    fun `rejects value above max`() {
        val rule = IntRangeRule(code = "C", message = "M", min = 1, max = 10)
        rule.validateForError(11).isValid shouldBe false
    }

    @Test
    @Description("default min=0 and max=Int.MAX_VALUE rejects negatives only")
    fun `defaults reject negatives`() {
        val rule = IntRangeRule(code = "C", message = "M")
        rule.validateForError(0).isValid shouldBe true
        rule.validateForError(Int.MAX_VALUE).isValid shouldBe true
        rule.validateForError(-1).isValid shouldBe false
    }
}
