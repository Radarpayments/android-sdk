package net.payrdr.mobile.payment.sdk.testUtils

object EmulatorSleep {
    fun sleep(time: Long = SLEEP_TIME) {
        Thread.sleep(time)
    }

    private const val SLEEP_TIME = 2_000L
}
