package net.payrdr.mobile.payment.sdk.core.utils

import androidx.test.filters.SmallTest
import io.qameta.allure.kotlin.Description
import org.junit.Assert.assertEquals
import org.junit.Test

@SmallTest
class BindingUtilsTest {

    @Test
    @Description("shouldCorrectlyExtractBindingId")
    fun shouldCorrectlyExtractBindingId() {
        val input = "pm_kF6D9zaGUjTGpFRojeb2VibyAX2TGCnikMsNdnp3XHb4opLEn"
        val result = BindingUtils.extractBindingId(input)
        val actual = "b089cf76-cb71-4907-89ee-29811f7a7b6c"

        assertEquals(result, actual)
    }

    @Test
    @Description("shouldReturnEmptyStringWhenInvalidInput")
    fun shouldReturnEmptyStringWhenInvalidInput() {
        val input = "kF6D9zaGUjTGpFRojeb2VibyAX2TGCnikMsNdnp3XHb4opLEn"
        val result = BindingUtils.extractBindingId(input)
        val actual = ""

        assertEquals(result, actual)
    }
}
