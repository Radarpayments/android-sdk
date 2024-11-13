package net.payrdr.mobile.payment.sdk.core

import android.util.Log
import net.payrdr.mobile.payment.sdk.logs.LogInterface
import net.payrdr.mobile.payment.sdk.logs.Source


object TestLoggerFactory {
    fun createLogcatLogger(tagMsg: String): LogInterface {
        return object : LogInterface {
            override fun debug(
                classMethod: Class<*>,
                tag: String,
                message: String,
                exception: Throwable?,
                source: Source
            ) {
                Log.d(tagMsg, "$classMethod $tag $message ${exception ?: ""}")
            }

            override fun error(
                classMethod: Class<*>,
                tag: String,
                message: String,
                exception: Throwable?,
                source: Source
            ) {
                Log.e(tagMsg, "$classMethod $tag $message ${exception ?: ""}")
            }

            override fun warning(
                classMethod: Class<*>,
                tag: String,
                message: String,
                exception: Throwable?,
                source: Source
            ) {
                Log.w(tagMsg, "$classMethod $tag $message ${exception ?: ""}")
            }

            override fun info(
                classMethod: Class<*>,
                tag: String,
                message: String,
                exception: Throwable?,
                source: Source
            ) {
                Log.i(tagMsg, "$classMethod $tag $message ${exception ?: ""}")
            }
            @Suppress("EmptyFunctionBlock")
            override fun uploadLogs() {}
        }
    }
}
