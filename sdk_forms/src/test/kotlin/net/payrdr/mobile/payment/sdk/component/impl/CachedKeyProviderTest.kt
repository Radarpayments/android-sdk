package net.payrdr.mobile.payment.sdk.component.impl

import android.content.SharedPreferences
import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.qameta.allure.kotlin.Description
import kotlinx.coroutines.test.runBlockingTest
import net.payrdr.mobile.payment.sdk.core.model.Key
import net.payrdr.mobile.payment.sdk.form.component.KeyProvider
import net.payrdr.mobile.payment.sdk.form.component.impl.CachedKeyProvider
import net.payrdr.mobile.payment.sdk.form.component.impl.CachedKeyProvider.Companion.KEY_DEFAULT_MAX_EXPIRE
import org.junit.Before
import org.junit.Test

@SmallTest
class CachedKeyProviderTest {

    private lateinit var cachedKeyProvider: CachedKeyProvider

    private val keyProvider = mockk<KeyProvider>()
    private val sharedPreferences = mockk<SharedPreferences>()

    @Before
    fun setUp() {
        cachedKeyProvider = CachedKeyProvider(keyProvider, sharedPreferences)
    }

    @Test
    @Description("provideKey should request key")
    fun `provideKey should request key`() = runBlockingTest {
        val expectedKey = Key(
            value = "key_value",
            protocol = "key_protocol",
            expiration = System.currentTimeMillis() + TEST_DELTA_TIME
        )
        every { sharedPreferences.contains("public_key_value") } returns false
        every { sharedPreferences.contains("public_key_protocol") } returns false
        every { sharedPreferences.contains("public_key_expiration") } returns false
        every { sharedPreferences.edit() } returns mockk(relaxed = true)
        coEvery { keyProvider.provideKey() } returns expectedKey

        val actualKey = cachedKeyProvider.provideKey()

        actualKey shouldBe expectedKey
    }

    @Test
    @Description("provideKey should return key from cache")
    fun `provideKey should return key from cache`() = runBlockingTest {
        val cachedKey = Key(
            value = "key_value",
            protocol = "key_protocol",
            expiration = System.currentTimeMillis() + TEST_DELTA_TIME
        )
        every { sharedPreferences.contains("public_key_value") } returns true
        every { sharedPreferences.contains("public_key_protocol") } returns true
        every { sharedPreferences.contains("public_key_expiration") } returns true
        every { sharedPreferences.getString("public_key_value", "") } returns cachedKey.value
        every { sharedPreferences.getString("public_key_protocol", "") } returns cachedKey.protocol
        every {
            sharedPreferences.getLong(
                "public_key_expiration",
                -1
            )
        } returns cachedKey.expiration
        every { sharedPreferences.edit() } returns mockk(relaxed = true)

        val actualKey = cachedKeyProvider.provideKey()

        actualKey shouldBe cachedKey
    }

    @Test
    @Description("provideKey should not use key from cache and request new one")
    fun `provideKey should not use key from cache and request new one`() = runBlockingTest {
        val cachedKey = Key(
            value = "key_value",
            protocol = "key_protocol",
            expiration = System.currentTimeMillis() - TEST_DELTA_TIME
        )
        val providedKey = Key(
            value = "key_value_provided",
            protocol = "key_protocol_provided",
            expiration = System.currentTimeMillis() + TEST_DELTA_TIME
        )
        every { sharedPreferences.contains("public_key_value") } returns true
        every { sharedPreferences.contains("public_key_protocol") } returns true
        every { sharedPreferences.contains("public_key_expiration") } returns true
        every { sharedPreferences.getString("public_key_value", "") } returns cachedKey.value
        every { sharedPreferences.getString("public_key_protocol", "") } returns cachedKey.protocol
        every {
            sharedPreferences.getLong(
                "public_key_expiration",
                -1
            )
        } returns cachedKey.expiration
        every { sharedPreferences.edit() } returns mockk(relaxed = true)
        coEvery { keyProvider.provideKey() } returns providedKey

        val actualKey = cachedKeyProvider.provideKey()

        actualKey shouldBe providedKey
    }

    @Test
    @Description("provideKey should save key with min of expired values - key expired")
    fun `provideKey should save key with min of expired values - key expired`() =
        runBlockingTest {
            val providedKey = Key(
                value = "key_value_provided",
                protocol = "key_protocol_provided",
                expiration = System.currentTimeMillis() + TEST_DELTA_TIME
            )
            every { sharedPreferences.contains("public_key_value") } returns false
            every { sharedPreferences.contains("public_key_protocol") } returns false
            every { sharedPreferences.contains("public_key_expiration") } returns false
            val editor = mockk<SharedPreferences.Editor> {
                every { this@mockk.remove(any()) } returns this
                every { this@mockk.putString(any(), any()) } returns this
                every { this@mockk.putLong(any(), any()) } returns this
                every { this@mockk.apply() } just runs
            }
            every { sharedPreferences.edit() } returns editor
            coEvery { keyProvider.provideKey() } returns providedKey

            val actualKey = cachedKeyProvider.provideKey()

            actualKey shouldBe providedKey
            verify {
                editor.remove(eq("public_key_value"))
                editor.remove(eq("public_key_protocol"))
                editor.remove("public_key_expiration")
                editor.apply()
                editor.putString("public_key_value", providedKey.value)
                editor.putString("public_key_protocol", providedKey.protocol)
                editor.putLong("public_key_expiration", providedKey.expiration)
                editor.apply()
            }
        }

    @Test
    @Description("provideKey should save key with min of expired values - max expired")
    fun `provideKey should save key with min of expired values - max expired`() =
        runBlockingTest {
            val providedKey = Key(
                value = "key_value_provided",
                protocol = "key_protocol_provided",
                expiration = System.currentTimeMillis() + KEY_DEFAULT_MAX_EXPIRE + TEST_DELTA_TIME
            )
            every { sharedPreferences.contains("public_key_value") } returns false
            every { sharedPreferences.contains("public_key_protocol") } returns false
            every { sharedPreferences.contains("public_key_expiration") } returns false
            val editor = mockk<SharedPreferences.Editor> {
                every { this@mockk.remove(any()) } returns this
                every { this@mockk.putString(any(), any()) } returns this
                every { this@mockk.putLong(any(), any()) } returns this
                every { this@mockk.apply() } just runs
            }
            every { sharedPreferences.edit() } returns editor
            coEvery { keyProvider.provideKey() } returns providedKey

            val actualKey = cachedKeyProvider.provideKey()

            actualKey.value shouldBe providedKey.value
            actualKey.protocol shouldBe providedKey.protocol
            actualKey.expiration shouldNotBe providedKey.expiration

            verify {
                editor.remove(eq("public_key_value"))
                editor.remove(eq("public_key_protocol"))
                editor.remove("public_key_expiration")
                editor.apply()
                editor.putString("public_key_value", providedKey.value)
                editor.putString("public_key_protocol", providedKey.protocol)
                editor.putLong("public_key_expiration", not(providedKey.expiration))
                editor.apply()
            }
        }

    companion object {
        private const val TEST_DELTA_TIME = 10_000
    }
}
