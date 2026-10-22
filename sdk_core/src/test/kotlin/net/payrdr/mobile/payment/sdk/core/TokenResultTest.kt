package net.payrdr.mobile.payment.sdk.core

import io.kotest.matchers.shouldBe
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.core.model.ParamField
import org.junit.Test

class TokenResultTest {

    @Test
    @Description("withToken stores token and empty errors map")
    fun `withToken stores token and empty errors`() {
        val result = TokenResult.withToken("abc.def.ghi")

        result.token shouldBe "abc.def.ghi"
        result.errors.isEmpty() shouldBe true
    }

    @Test
    @Description("withErrors stores errors map and null token")
    fun `withErrors stores errors and nulls token`() {
        val errors = mapOf(
            ParamField.PAN to "invalid pan",
            ParamField.CVC to "invalid cvc"
        )

        val result = TokenResult.withErrors(errors)

        result.token shouldBe null
        result.errors shouldBe errors
    }

    @Test
    @Description("withErrors tolerates empty error map")
    fun `withErrors tolerates empty error map`() {
        val result = TokenResult.withErrors(emptyMap())

        result.token shouldBe null
        result.errors.isEmpty() shouldBe true
    }

    @Test
    @Description("two TokenResults built from the same token are equal")
    fun `token results are data-class equal`() {
        val first = TokenResult.withToken("same")
        val second = TokenResult.withToken("same")

        (first == second) shouldBe true
        (first.hashCode() == second.hashCode()) shouldBe true
    }
}
