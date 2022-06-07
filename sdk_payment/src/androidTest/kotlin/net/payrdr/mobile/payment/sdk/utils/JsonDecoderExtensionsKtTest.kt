package net.payrdr.mobile.payment.sdk.utils

import android.Manifest
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@SmallTest
@RunWith(AllureAndroidJUnit4::class)
class JsonDecoderExtensionsKtTest {

    @get:Rule
    val permissionRule: TestRule =
        GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private val jsonObject: JSONObject = JSONObject(
        """
            {
                "int":12,
                "long":452548843,
                "double":457.45677,
                "float":4.78,
                "string":"JsonDecoder",
                "boolean":true,
                "object": {
                    "value":5 
                }
            }
        """.trimIndent()
    )

    @Test
    @Description("shouldReturnIntValueFromJson")
    fun shouldReturnIntValueFromJson() {
        val res: Int? = jsonObject.optValue("int")
        assertEquals(12, res)
    }

    @Test
    @Description("shouldReturnLongValueFromJson")
    fun shouldReturnLongValueFromJson() {
        val res: Long? = jsonObject.optValue("long")
        assertEquals(452548843L, res)
    }

    @Test
    @Description("shouldReturnDoubleValueFromJson")
    fun shouldReturnDoubleValueFromJson() {
        val res: Double? = jsonObject.optValue("double")
        assertEquals(457.45677, res)
    }

    @Test
    @Description("shouldReturnFloatValueFromJson")
    fun shouldReturnFloatValueFromJson() {
        val res: Float? = jsonObject.optValue("float")
        assertEquals(4.78F, res)
    }

    @Test
    @Description("shouldReturnStringValueFromJson")
    fun shouldReturnStringValueFromJson() {
        val res: String? = jsonObject.optValue("string")
        assertEquals("JsonDecoder", res)
    }

    @Test
    @Description("shouldReturnBooleanValueFromJson")
    fun shouldReturnBooleanValueFromJson() {
        val res: Boolean? = jsonObject.optValue("boolean")
        assertEquals(true, res)
    }

    @Test
    @Description("shouldReturnDefaultValueFromJson")
    fun shouldReturnDefaultValueFromJson() {
        val res: Int? = jsonObject.optValue("project")
        assertEquals(null, res)
    }

    @Test(expected = IllegalArgumentException::class)
    @Description("shouldReturnErrorValueFromJson")
    fun shouldReturnErrorValueFromJson() {
        val res: Number? = jsonObject.optValue("object")
        assertEquals(Number(5), res)
    }

    data class Number(val value: Int)
}
