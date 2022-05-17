package net.payrdr.mobile.payment.sdk.utils

import org.json.JSONObject

/**
 * Gets the fields of a JSON object for any data type.
 *
 * @param name - field name.
 * @param default - default value for field.
 */
inline fun <reified T> JSONObject.optValue(
    name: String,
    default: T? = null
): T? = if (isNull(name)) {
    default
} else {
    when (T::class) {
        Int::class -> getInt(name) as T
        Long::class -> getLong(name) as T
        Double::class -> getDouble(name) as T
        Float::class -> getDouble(name).toFloat() as T
        String::class -> getString(name) as T
        Boolean::class -> getBoolean(name) as T
        else -> throw IllegalArgumentException("Incorrect field type ${T::class.simpleName}")
    }
}
