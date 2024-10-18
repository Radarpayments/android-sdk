package net.payrdr.mobile.payment.sdk.utils

/**
 * Checking entry of one of the words list in a string.
 *
 * @param keywords list of words.
 */
fun String.containsAnyOfKeywordIgnoreCase(keywords: List<OrderStatus>): Boolean {
    return keywords.any {
        it.statusName.equals(this, ignoreCase = true)
    }
}

/**
 * Creating string which looks like json from map.
 */
fun Map<String, String>.mapToJsonString(): String {
    return this.entries.joinToString(
        prefix = "{",
        postfix = "}",
        separator = ","
    ) { (key, value) -> "\"$key\":\"$value\"" }
}
