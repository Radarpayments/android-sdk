package net.payrdr.mobile.payment.sdk.utils

/**
 * Checking entry of one of the words list in a string.
 *
 * @param keywords list of words.
 */
fun String.containsAnyOfKeywordIgnoreCase(keywords: List<String>): Boolean {
    for (keyword in keywords) {
        if (this.contains(keyword, true)) {
            return true
        }
    }
    return false
}
