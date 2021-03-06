package net.payrdr.mobile.payment.sdk.form.ui.helper

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.payrdr.mobile.payment.sdk.core.utils.digitsOnly
import net.payrdr.mobile.payment.sdk.form.component.CardInfo
import net.payrdr.mobile.payment.sdk.form.component.CardInfoProvider
import net.payrdr.mobile.payment.sdk.form.ui.widget.BankCardView

internal class CardResolver(
    private val bankCardView: BankCardView,
    private val cardInfoProvider: CardInfoProvider
) {
    private var prevBin: String = ""
    private var currentJob: Job? = null
    private val workScope: CoroutineScope = CoroutineScope(Dispatchers.Main + Job())

    fun resolve(number: String, withDelay: Boolean = false) {
        val clearNumber = number.digitsOnly()
        val hasMinLength = clearNumber.length >= CARD_BIN_MIN_LENGTH
        if (hasMinLength) {
            val bin = clearNumber.substring(0, clearNumber.length.coerceAtMost(CARD_BIN_MAX_LENGTH))
            if (bin != prevBin) {
                resolveCardInfo(withDelay, bin)
            }
            prevBin = bin
        } else {
            executeJob { bankCardView.setupUnknownBrand() }
        }
    }

    private fun resolveCardInfo(withDelay: Boolean, bin: String) {
        executeJob {
            if (withDelay) {
                delay(RESOLVE_DELAY)
            }
            bankCardView.setupCardInfo(loadCardInfo(bin))
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun loadCardInfo(bin: String): CardInfo? = try {
        withContext(Dispatchers.IO) { cardInfoProvider.resolve(bin) }
    } catch (e: Exception) {
        Log.e("PAYRDRSDK", e.message ?: e.toString())
        null
    }

    private fun executeJob(block: suspend () -> Unit) {
        synchronized(workScope) {
            currentJob?.cancel()
            currentJob = workScope.launch {
                block()
            }
        }
    }

    private fun BankCardView.setupCardInfo(cardInfo: CardInfo?) {
        cardInfo?.let { info ->
            setBankLogoUrl(info.logoMini)
        } ?: setupUnknownBrand()
    }

    companion object {
        private const val CARD_BIN_MIN_LENGTH = 6
        private const val CARD_BIN_MAX_LENGTH = 8
        private const val RESOLVE_DELAY = 500L
    }
}
