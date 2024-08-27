package net.payrdr.mobile.payment.sdk.test

import net.payrdr.mobile.payment.sdk.core.model.MSDKRegisteredFrom
import net.payrdr.mobile.payment.sdk.form.model.AdditionalField
import net.payrdr.mobile.payment.sdk.form.model.CameraScannerOptions
import net.payrdr.mobile.payment.sdk.form.model.CardDeleteOptions
import net.payrdr.mobile.payment.sdk.form.model.CardSaveOptions
import net.payrdr.mobile.payment.sdk.form.model.HolderInputOptions
import net.payrdr.mobile.payment.sdk.form.model.NfcScannerOptions
import net.payrdr.mobile.payment.sdk.form.model.PaymentConfig
import net.payrdr.mobile.payment.sdk.form.model.Theme
import java.util.Locale
import java.util.UUID

object PaymentConfigTestProvider {

    fun defaultConfig(): PaymentConfig = PaymentConfig(
        order = UUID.randomUUID().toString(),
        uuid = UUID.randomUUID().toString(),
        timestamp = System.currentTimeMillis(),
        buttonText = null,
        locale = Locale.getDefault(),
        cards = emptySet(),
        cardSaveOptions = CardSaveOptions.HIDE,
        holderInputOptions = HolderInputOptions.HIDE,
        cameraScannerOptions = CameraScannerOptions.ENABLED,
        theme = Theme.SYSTEM,
        nfcScannerOptions = NfcScannerOptions.ENABLED,
        storedPaymentMethodCVCRequired = true,
        cardDeleteOptions = CardDeleteOptions.NO_DELETE,
        registeredFrom = MSDKRegisteredFrom.MSDK_CORE,
        fieldsNeedToBeFilledForMastercard = emptyList(),
        fieldsNeedToBeFilledForVisa = emptyList()
    )

    fun configWithAllAdditionalCardParams(): PaymentConfig = defaultConfig().copy(
        fieldsNeedToBeFilledForVisa = listOf(
            AdditionalField(fieldName = "MOBILE_PHONE", isMandatory = true, prefilledValue = null),
            AdditionalField(fieldName = "EMAIL", isMandatory = true, prefilledValue = null),
            AdditionalField(fieldName = "BILLING_CITY", isMandatory = true, prefilledValue = null),
            AdditionalField(
                fieldName = "BILLING_COUNTRY",
                isMandatory = true,
                prefilledValue = null
            ),
            AdditionalField(fieldName = "BILLING_STATE", isMandatory = true, prefilledValue = null),
            AdditionalField(
                fieldName = "BILLING_POSTAL_CODE",
                isMandatory = true,
                prefilledValue = null
            ),
            AdditionalField(
                fieldName = "BILLING_ADDRESS_LINE1",
                isMandatory = true,
                prefilledValue = null
            ),
            AdditionalField(
                fieldName = "BILLING_ADDRESS_LINE2",
                isMandatory = true,
                prefilledValue = null
            ),
            AdditionalField(
                fieldName = "BILLING_ADDRESS_LINE3",
                isMandatory = true,
                prefilledValue = null
            ),
        )
    )
}
