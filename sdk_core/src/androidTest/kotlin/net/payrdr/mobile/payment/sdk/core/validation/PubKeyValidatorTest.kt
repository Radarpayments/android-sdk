package net.payrdr.mobile.payment.sdk.core.validation

import android.Manifest
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule.grant
import io.qameta.allure.kotlin.Description
import net.payrdr.mobile.payment.sdk.core.R
import net.payrdr.mobile.payment.sdk.core.test.getString
import net.payrdr.mobile.payment.sdk.core.test.targetContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@SmallTest
class PubKeyValidatorTest {

    @get:Rule
    val permissionRule: TestRule = grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private lateinit var pubKeyValidator: PubKeyValidator

    @Suppress("MaxLineLength")
    private val testPubKey =
        "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAij/G3JVV3TqYCFZTPmwi4JduQMsZ2HcFLBBA9fYAvApv3FtA+zKdUGgKh/OPbtpsxe1C57gIaRclbzMoafTb0eOdj+jqSEJMlVJYSiZ8Hn6g67evhu9wXh5ZKBQ1RUpqL36LbhYnIrP+TEGR/VyjbC6QTfaktcRfa8zRqJczHFsyWxnlfwKLfqKz5wSqXkShcrwcfRJCyDRjZX6OFUECHsWVK3WMcOV3WZREwbCkh/o5R5Vl6xoyLvSqVEKQiHupJcZu9UEOJiP3yNCn9YPgyFs2vrCeg6qxDPFnCfetcDCLjjLenGF7VyZzBJ9G2NP3k/mNVtD8Kl7lpiurwY7EZwIDAQAB-----END PUBLIC KEY-----"

    @Before
    fun setUp() {
        pubKeyValidator = PubKeyValidator(targetContext())
    }

    @Test
    @Description("shouldAcceptCorrectPubKey")
    fun shouldAcceptCorrectPubKey() {
        val result = pubKeyValidator.validate(testPubKey)

        assertEquals(true, result.isValid)
        assertNull(result.errorMessage)
        assertNull(result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptEmptyPubKey")
    fun shouldNotAcceptEmptyPubKey() {
        val result = pubKeyValidator.validate("")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_pub_key_required), result.errorMessage)
        assertEquals(ValidationCodes.required, result.errorCode)
    }

    @Test
    @Description("shouldNotAcceptBlankPubKey")
    fun shouldNotAcceptBlankPubKey() {
        val result = pubKeyValidator.validate("        ")

        assertEquals(false, result.isValid)
        assertEquals(getString(R.string.payrdr_pub_key_required), result.errorMessage)
        assertEquals(ValidationCodes.required, result.errorCode)
    }
}
