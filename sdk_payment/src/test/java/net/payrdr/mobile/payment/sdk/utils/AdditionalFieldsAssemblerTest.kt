package net.payrdr.mobile.payment.sdk.utils

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import net.payrdr.mobile.payment.sdk.api.entity.BillingPayerData
import net.payrdr.mobile.payment.sdk.api.entity.CustomerDetails
import net.payrdr.mobile.payment.sdk.api.entity.FieldParams
import net.payrdr.mobile.payment.sdk.api.entity.OrderPayerData
import net.payrdr.mobile.payment.sdk.model.FilledAdditionalPayerParams
import org.junit.Assert.assertThrows
import org.junit.Test

@SmallTest
class AdditionalFieldsAssemblerTest {

    private val emptyBilling = BillingPayerData(null, null, null, null, null, null, null)
    private val emptyCustomer = CustomerDetails(email = null, phone = null)
    private val emptyOrderPayer = OrderPayerData(mobilePhone = null)

    // ---- assembleAdditionalFieldsForPayments ----

    @Test
    fun shouldReturnEmptyListsWhenBothParamsNull() {
        val result = AdditionalFieldsAssembler.assembleAdditionalFieldsForPayments(
            visaParamsNeedToBeFilled = null,
            mastercardParamsNeedToBeFilled = null,
            customerDetails = emptyCustomer,
            orderPayerData = emptyOrderPayer,
            billingPayerData = emptyBilling
        )

        result.visaFields.isEmpty() shouldBe true
        result.mastercardFields.isEmpty() shouldBe true
    }

    @Test
    fun shouldAssembleVisaFieldsOnly() {
        val visaParams = listOf(FieldParams(isMandatory = true, name = "EMAIL"))

        val result = AdditionalFieldsAssembler.assembleAdditionalFieldsForPayments(
            visaParamsNeedToBeFilled = visaParams,
            mastercardParamsNeedToBeFilled = null,
            customerDetails = CustomerDetails(email = "user@example.com", phone = null),
            orderPayerData = emptyOrderPayer,
            billingPayerData = emptyBilling
        )

        result.visaFields.size shouldBe 1
        result.visaFields[0].fieldName shouldBe "EMAIL"
        result.visaFields[0].isMandatory shouldBe true
        result.visaFields[0].prefilledValue shouldBe "user@example.com"
        result.mastercardFields.isEmpty() shouldBe true
    }

    @Test
    fun shouldAssembleMastercardFieldsOnly() {
        val mcParams = listOf(FieldParams(isMandatory = false, name = "MOBILE_PHONE"))

        val result = AdditionalFieldsAssembler.assembleAdditionalFieldsForPayments(
            visaParamsNeedToBeFilled = null,
            mastercardParamsNeedToBeFilled = mcParams,
            customerDetails = emptyCustomer,
            orderPayerData = OrderPayerData(mobilePhone = "+79991234567"),
            billingPayerData = emptyBilling
        )

        result.mastercardFields.size shouldBe 1
        result.mastercardFields[0].fieldName shouldBe "MOBILE_PHONE"
        result.mastercardFields[0].isMandatory shouldBe false
        result.mastercardFields[0].prefilledValue shouldBe "+79991234567"
        result.visaFields.isEmpty() shouldBe true
    }

    @Test
    fun shouldAssembleBothVisaAndMastercardFields() {
        val visaParams = listOf(FieldParams(isMandatory = true, name = "EMAIL"))
        val mcParams = listOf(FieldParams(isMandatory = false, name = "BILLING_CITY"))

        val result = AdditionalFieldsAssembler.assembleAdditionalFieldsForPayments(
            visaParamsNeedToBeFilled = visaParams,
            mastercardParamsNeedToBeFilled = mcParams,
            customerDetails = CustomerDetails(email = "a@b.com", phone = null),
            orderPayerData = emptyOrderPayer,
            billingPayerData = BillingPayerData(null, null, null, "Moscow", null, null, null)
        )

        result.visaFields.size shouldBe 1
        result.visaFields[0].prefilledValue shouldBe "a@b.com"
        result.mastercardFields.size shouldBe 1
        result.mastercardFields[0].prefilledValue shouldBe "Moscow"
    }

    @Test
    fun shouldPrefillEmailFromCustomerDetails() {
        val params = listOf(FieldParams(isMandatory = true, name = "EMAIL"))

        val result = AdditionalFieldsAssembler.assembleAdditionalFieldsForPayments(
            visaParamsNeedToBeFilled = params,
            mastercardParamsNeedToBeFilled = null,
            customerDetails = CustomerDetails(email = "test@test.com", phone = null),
            orderPayerData = emptyOrderPayer,
            billingPayerData = emptyBilling
        )

        result.visaFields[0].prefilledValue shouldBe "test@test.com"
    }

    @Test
    fun shouldPrefillMobilePhoneFromOrderPayerDataWhenPresent() {
        val params = listOf(FieldParams(isMandatory = false, name = "MOBILE_PHONE"))

        val result = AdditionalFieldsAssembler.assembleAdditionalFieldsForPayments(
            visaParamsNeedToBeFilled = params,
            mastercardParamsNeedToBeFilled = null,
            customerDetails = CustomerDetails(email = null, phone = "+70001111111"),
            orderPayerData = OrderPayerData(mobilePhone = "+79991234567"),
            billingPayerData = emptyBilling
        )

        // orderPayerData.mobilePhone takes priority over customerDetails.phone
        result.visaFields[0].prefilledValue shouldBe "+79991234567"
    }

    @Test
    fun shouldFallbackToCustomerPhoneWhenOrderPayerDataPhoneNull() {
        val params = listOf(FieldParams(isMandatory = false, name = "MOBILE_PHONE"))

        val result = AdditionalFieldsAssembler.assembleAdditionalFieldsForPayments(
            visaParamsNeedToBeFilled = params,
            mastercardParamsNeedToBeFilled = null,
            customerDetails = CustomerDetails(email = null, phone = "+70002222222"),
            orderPayerData = OrderPayerData(mobilePhone = null),
            billingPayerData = emptyBilling
        )

        result.visaFields[0].prefilledValue shouldBe "+70002222222"
    }

    @Test
    fun shouldPrefillAllBillingFields() {
        val params = listOf(
            FieldParams(isMandatory = true, name = "BILLING_COUNTRY"),
            FieldParams(isMandatory = false, name = "BILLING_CITY"),
            FieldParams(isMandatory = false, name = "BILLING_STATE"),
            FieldParams(isMandatory = false, name = "BILLING_POSTAL_CODE"),
            FieldParams(isMandatory = false, name = "BILLING_ADDRESS_LINE1"),
            FieldParams(isMandatory = false, name = "BILLING_ADDRESS_LINE2"),
            FieldParams(isMandatory = false, name = "BILLING_ADDRESS_LINE3"),
        )
        val billing = BillingPayerData(
            billingAddressLine1 = "Line1",
            billingAddressLine2 = "Line2",
            billingAddressLine3 = "Line3",
            billingCity = "Moscow",
            billingCountry = "RU",
            billingPostalCode = "123456",
            billingState = "Moscow Oblast"
        )

        val result = AdditionalFieldsAssembler.assembleAdditionalFieldsForPayments(
            visaParamsNeedToBeFilled = params,
            mastercardParamsNeedToBeFilled = null,
            customerDetails = emptyCustomer,
            orderPayerData = emptyOrderPayer,
            billingPayerData = billing
        )

        val fieldMap = result.visaFields.associate { it.fieldName to it.prefilledValue }
        fieldMap["BILLING_COUNTRY"] shouldBe "RU"
        fieldMap["BILLING_CITY"] shouldBe "Moscow"
        fieldMap["BILLING_STATE"] shouldBe "Moscow Oblast"
        fieldMap["BILLING_POSTAL_CODE"] shouldBe "123456"
        fieldMap["BILLING_ADDRESS_LINE1"] shouldBe "Line1"
        fieldMap["BILLING_ADDRESS_LINE2"] shouldBe "Line2"
        fieldMap["BILLING_ADDRESS_LINE3"] shouldBe "Line3"
    }

    @Test
    fun shouldThrowWhenUnknownFieldName() {
        val params = listOf(FieldParams(isMandatory = false, name = "UNKNOWN_FIELD"))

        assertThrows(IllegalStateException::class.java) {
            AdditionalFieldsAssembler.assembleAdditionalFieldsForPayments(
                visaParamsNeedToBeFilled = params,
                mastercardParamsNeedToBeFilled = null,
                customerDetails = emptyCustomer,
                orderPayerData = emptyOrderPayer,
                billingPayerData = emptyBilling
            )
        }
    }

    // ---- assembleFilledParams ----

    @Test
    fun shouldReturnEmptyMapWhenAllFilledParamsNull() {
        val params = FilledAdditionalPayerParams(null, null, null, null, null, null, null, null, null)

        val result = AdditionalFieldsAssembler.assembleFilledParams(params)

        result.isEmpty() shouldBe true
    }

    @Test
    fun shouldMapNonNullFilledParamsToCorrectKeys() {
        val params = FilledAdditionalPayerParams(
            city = "Moscow",
            country = "RU",
            postalCode = "123456",
            state = "MO",
            addressLine1 = "Addr1",
            addressLine2 = "Addr2",
            addressLine3 = null,
            phone = null,
            email = null
        )

        val result = AdditionalFieldsAssembler.assembleFilledParams(params)

        result["billingCity"] shouldBe "Moscow"
        result["billingCountry"] shouldBe "RU"
        result["billingPostalCode"] shouldBe "123456"
        result["billingState"] shouldBe "MO"
        result["billingAddressLine1"] shouldBe "Addr1"
        result["billingAddressLine2"] shouldBe "Addr2"
        result.containsKey("billingAddressLine3") shouldBe false
    }

    @Test
    fun shouldExcludeNullValuesFromFilledParams() {
        val params = FilledAdditionalPayerParams(
            city = "SPb",
            country = null,
            postalCode = null,
            state = null,
            addressLine1 = null,
            addressLine2 = null,
            addressLine3 = null,
            phone = null,
            email = null
        )

        val result = AdditionalFieldsAssembler.assembleFilledParams(params)

        result.size shouldBe 1
        result["billingCity"] shouldBe "SPb"
    }
}
