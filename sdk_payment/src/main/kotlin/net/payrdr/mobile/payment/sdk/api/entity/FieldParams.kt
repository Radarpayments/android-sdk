package net.payrdr.mobile.payment.sdk.api.entity

/**
 * Information about field for card.
 * @param isMandatory is mandatory this field or not.
 * @param name field name.
 */
data class FieldParams(
    val isMandatory: Boolean,
    val name: String
)
