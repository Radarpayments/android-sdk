package net.payrdr.mobile.payment.sdk.payment

import com.kaspersky.kaspresso.annotations.ScreenShooterTest
import io.kotest.matchers.shouldBe
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.core.BaseTestCase
import net.payrdr.mobile.payment.sdk.data.TestCardHelper
import net.payrdr.mobile.payment.sdk.logs.Logger
import net.payrdr.mobile.payment.sdk.logs.sentry.SentryLogUploader
import net.payrdr.mobile.payment.sdk.logs.sentry.SentryLogUploaderConfig
import net.payrdr.mobile.payment.sdk.logs.sentry.SentryLogger
import net.payrdr.mobile.payment.sdk.payment.model.CheckoutConfig
import net.payrdr.mobile.payment.sdk.screen.BottomSheetScreen
import net.payrdr.mobile.payment.sdk.screen.NewCardScreen
import net.payrdr.mobile.payment.sdk.screen.ThreeDS2Screen
import net.payrdr.mobile.payment.sdk.screen.clickOnNewCard
import net.payrdr.mobile.payment.sdk.screen.fillOutFormAndSend
import org.junit.Ignore
import org.junit.Test

class PaymentThreeDSLogUseCaseTest: BaseTestCase() {

    @ScreenShooterTest
    @Ignore("Need URL and Key")
    @Test
    // DO NOT COMMIT URL AND KEY
    fun shouldReturnErrorPaymentWithNewCardFullThreeDSUse3DS2SDKWithLog() {
        Logger.addLogInterface(
            SentryLogger(
                SentryLogUploader(
                    logUploaderConfig = SentryLogUploaderConfig(
                        url = "SentryURL",
                        key = "SentryKey",
                        appId = "sdkAppId",
                    ),
                    installationIdProvider = { "installationId" }
                ),
                isWebViewLogsEnabled = true,
            )
        )

        val mdOrder: String = testOrderHelper.registerOrder()
        val config = CheckoutConfig.MdOrder(mdOrder)
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig.copy(use3DSConfig = testConfigForUse3DS2sdk))
                SDKPayment.checkout(testActivity, config)
            }
            step("Click on new card button") {
                BottomSheetScreen {
                    clickOnNewCard()
                }
            }
            step("Fill new card form") {
                NewCardScreen {
                    fillOutFormAndSend(TestCardHelper.cardSuccessFull3DS2)
                }
            }
            step("Input verification code attempt #1") {
                ThreeDS2Screen {
                    fillOutFormAndSend(TestCardHelper.invalidVerificationCode)
                }
            }
            step("Input verification code attempt #2") {
                ThreeDS2Screen {
                    fillOutFormAndSend(TestCardHelper.invalidVerificationCode)
                }
            }
            step("Input verification code attempt #3") {
                ThreeDS2Screen {
                    fillOutFormAndSend(TestCardHelper.invalidVerificationCode)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.isSuccess shouldBe false
                }
                Thread.sleep(60_000)
            }
        }
    }
}
