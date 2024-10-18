package net.payrdr.mobile.payment.sdk.utils

import net.payrdr.mobile.payment.sdk.api.entity.BillingPayerData
import net.payrdr.mobile.payment.sdk.api.entity.CustomerDetails
import net.payrdr.mobile.payment.sdk.api.entity.FieldParams
import net.payrdr.mobile.payment.sdk.api.entity.OrderPayerData
import net.payrdr.mobile.payment.sdk.form.model.AdditionalField
import net.payrdr.mobile.payment.sdk.form.model.AdditionalFieldsForPaymentSystem
import net.payrdr.mobile.payment.sdk.form.model.FilledAdditionalPayerParams
import java.lang.IllegalStateException

/**
 * Class for assembling additional information about payer.
 */
object AdditionalFieldsAssembler {

    /**
     * Assemble additional fields for VISA and MASTERCARD payment systems.
     * @param visaParamsNeedToBeFilled params which need to be filled by payer for VISA.
     * @param mastercardParamsNeedToBeFilled params which need to be filled by payer for MASTERCARD.
     * @param customerDetails customer information.
     * @param billingPayerData prefilled information about customer.
     * @param orderPayerData customer information.
     * @return [AdditionalFieldsForPaymentSystem]
     */
    fun assembleAdditionalFieldsForPayments(
        visaParamsNeedToBeFilled: List<FieldParams>?,
        mastercardParamsNeedToBeFilled: List<FieldParams>?,
        customerDetails: CustomerDetails,
        orderPayerData: OrderPayerData,
        billingPayerData: BillingPayerData
    ): AdditionalFieldsForPaymentSystem {
        val visaParams = assembleFields(
            visaParamsNeedToBeFilled,
            customerDetails,
            orderPayerData,
            billingPayerData
        )
        val mastercardParams = assembleFields(
            mastercardParamsNeedToBeFilled,
            customerDetails,
            orderPayerData,
            billingPayerData
        )
        return AdditionalFieldsForPaymentSystem(
            visaFields = visaParams,
            mastercardFields = mastercardParams
        )
    }

    private fun assembleFields(
        paramsNeedToBeFilled: List<FieldParams>?,
        customerDetails: CustomerDetails,
        orderPayerData: OrderPayerData,
        billingPayerData: BillingPayerData
    ): List<AdditionalField> {
        val additionalParamsList = mutableListOf<AdditionalField>()
        paramsNeedToBeFilled?.forEach { param ->
            val value = setValueFromPrefilledParams(
                param.name,
                billingPayerData,
                customerDetails,
                orderPayerData
            )
            additionalParamsList.add(
                AdditionalField(
                    fieldName = param.name,
                    isMandatory = param.isMandatory,
                    prefilledValue = value
                )
            )
        }
        return additionalParamsList
    }

    private fun setValueFromPrefilledParams(
        name: String,
        billingPayerData: BillingPayerData,
        customerDetails: CustomerDetails,
        orderPayerData: OrderPayerData
    ): String? {
        return when (name) {
            "EMAIL" -> customerDetails.email
            "MOBILE_PHONE" -> {
                orderPayerData.mobilePhone ?: customerDetails.phone
            }

            "BILLING_COUNTRY" -> billingPayerData.billingCountry
            "BILLING_CITY" -> billingPayerData.billingCity
            "BILLING_STATE" -> billingPayerData.billingState
            "BILLING_POSTAL_CODE" -> billingPayerData.billingPostalCode
            "BILLING_ADDRESS_LINE1" -> billingPayerData.billingAddressLine1
            "BILLING_ADDRESS_LINE2" -> billingPayerData.billingAddressLine2
            "BILLING_ADDRESS_LINE3" -> billingPayerData.billingAddressLine3
            else -> throw IllegalStateException()
        }
    }

    fun assembleFilledParams(
        filledAdditionalPayerParams: FilledAdditionalPayerParams
    ): Map<String, String> {
        val result = mutableMapOf<String, String>()
        if (filledAdditionalPayerParams.city != null) result["billingCity"] =
            filledAdditionalPayerParams.city!!
        if (filledAdditionalPayerParams.country != null) result["billingCountry"] =
            filledAdditionalPayerParams.country!!
        if (filledAdditionalPayerParams.addressLine1 != null) result["billingAddressLine1"] =
            filledAdditionalPayerParams.addressLine1!!
        if (filledAdditionalPayerParams.addressLine2 != null) result["billingAddressLine2"] =
            filledAdditionalPayerParams.addressLine2!!
        if (filledAdditionalPayerParams.addressLine3 != null) result["billingAddressLine3"] =
            filledAdditionalPayerParams.addressLine3!!
        if (filledAdditionalPayerParams.postalCode != null) result["billingPostalCode"] =
            filledAdditionalPayerParams.postalCode!!
        if (filledAdditionalPayerParams.state != null) result["billingState"] =
            filledAdditionalPayerParams.state!!
        return result
    }
}
