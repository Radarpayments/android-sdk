package net.payrdr.mobile.payment.sdk.form.utils

import net.payrdr.mobile.payment.sdk.form.SDKException
import org.json.JSONArray
import org.json.JSONObject

/**
 * Representation of an array of json objects as a list.
 *
 * @return returns the [JSONObject] list based on the [JSONArray] data.
 */
fun JSONArray.asList(): List<JSONObject> =
    (0 until length()).asSequence().map { get(it) as JSONObject }.toList()

/**
 * Representation of an array of json objects as a list of terms.
 *
 * @return returns a list [String] based on the data [JSONArray].
 */
fun JSONArray.asStringList(): List<String> =
    (0 until length()).asSequence().map { get(it) as String }.toList()

/**
 * Checks the presence of a field value
 *
 * @return returns the value if it is not null, otherwise, throw an exception.
 */
fun <T> T?.requiredField(fieldName: String): T {
    if (this == null) {
        throw SDKException(message = "Required field $fieldName is null", cause = null)
    } else {
        return this
    }
}
