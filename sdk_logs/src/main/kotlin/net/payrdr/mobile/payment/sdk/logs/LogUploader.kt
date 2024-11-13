package net.payrdr.mobile.payment.sdk.logs

interface LogUploader {

    fun uploadLogs(logs: List<String>)
}
