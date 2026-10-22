package net.payrdr.mobile.payment.sdk.core.validation.rules

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import org.junit.Test

@SmallTest
class RegExRuleTest {

    @Test
    @Description("string matching regex is valid")
    fun `accepts matching string`() {
        val rule = RegExRule(code = "C", message = "M", regex = Regex("^[0-9]{3}\$"))
        rule.validateForError("123").isValid shouldBe true
    }

    @Test
    @Description("string not matching regex fails with code/message")
    fun `rejects non-matching string`() {
        val rule = RegExRule(code = "C", message = "M", regex = Regex("^[0-9]{3}\$"))
        val result = rule.validateForError("12")
        result.isValid shouldBe false
        result.errorCode shouldBe "C"
        result.errorMessage shouldBe "M"
    }

    @Test
    @Description("empty string is rejected when regex requires characters")
    fun `rejects empty when regex requires content`() {
        val rule = RegExRule(code = "C", message = "M", regex = Regex(".+"))
        rule.validateForError("").isValid shouldBe false
    }
}
