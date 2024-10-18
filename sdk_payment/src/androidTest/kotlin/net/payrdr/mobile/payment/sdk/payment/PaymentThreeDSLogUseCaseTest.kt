package net.payrdr.mobile.payment.sdk.payment

import com.kaspersky.kaspresso.annotations.ScreenShooterTest
import io.kotest.matchers.shouldBe
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.core.BaseTestCase
import net.payrdr.mobile.payment.sdk.data.TestCardHelper
import net.payrdr.mobile.payment.sdk.screen.BottomSheetScreen
import net.payrdr.mobile.payment.sdk.screen.NewCardScreen
import net.payrdr.mobile.payment.sdk.screen.ThreeDS2Screen
import net.payrdr.mobile.payment.sdk.screen.clickOnNewCard
import net.payrdr.mobile.payment.sdk.screen.fillOutFormAndSend
import net.payrdr.mobile.payment.sdk.threeds.ThreeDSLogger
import net.payrdr.mobile.payment.sdk.threeds.logs.sentry.SentryLogUploaderConfig
import org.junit.Ignore
import org.junit.Test

class PaymentThreeDSLogUseCaseTest: BaseTestCase() {

    @ScreenShooterTest
    @Ignore
    @Test
    fun shouldReturnErrorPaymentWithNewCardFullThreeDSUse3DS2SDKWithLog() {
        ThreeDSLogger.INSTANCE.setupLogUploaderConfigProvider { sdkAppId ->
            // TODO do not commit url and key!!!
            SentryLogUploaderConfig.Builder()
                .withUrl("SentryURL")
                .withKey("SentryKey")
                .build()
        }

        val mdOrder: String = testOrderHelper.registerOrder()
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig.copy(use3DSConfig = testConfigForUse3DS2sdk))
                SDKPayment.checkout(testActivity, mdOrder)
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
