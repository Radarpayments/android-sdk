package net.payrdr.mobile.payment.sdk.core

import net.payrdr.mobile.payment.sdk.core.model.ParamField

/**
 *  Token description.
 *
 *  @param token token as string.
 *  @param errors error while generating token.
 */
data class TokenResult private constructor(
    val token: String?,
    val errors: Map<ParamField, String>
) {

    companion object {
        /**
         *  Method for returning a token.
         *
         *  @param token generated token.
         *  @return received token.
         */
        fun withToken(token: String): TokenResult = TokenResult(token = token, errors = emptyMap())

        /** Method to return an error.
         *
         *  @param errors errors with their description obtained during the generation of the token.
         *  @return error while generating token.
         */
        fun withErrors(errors: Map<ParamField, String>): TokenResult =
            TokenResult(token = null, errors = errors)
    }
}
