package net.payrdr.mobile.payment.sdk.test

object SleepEmulator {

    fun sleep() {
        Thread.sleep(SLEEP_TIME)
    }

    private const val SLEEP_TIME = 2_000L
}
