package net.payrdr.mobile.payment.sdk.payment

import com.kaspersky.kaspresso.annotations.ScreenShooterTest
import io.kotest.matchers.shouldBe
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.core.BaseTestCase
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.cardWith3DSWithPaRes
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.invalidVerificationCodePaRes
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.validVerificationCodePaRes
import net.payrdr.mobile.payment.sdk.payment.model.CheckoutConfig
import net.payrdr.mobile.payment.sdk.screen.BottomSheetScreen
import net.payrdr.mobile.payment.sdk.screen.NewCardScreen
import net.payrdr.mobile.payment.sdk.screen.ThreeDS1Screen
import net.payrdr.mobile.payment.sdk.screen.clickCancel
import net.payrdr.mobile.payment.sdk.screen.clickOnNewCard
import net.payrdr.mobile.payment.sdk.screen.clickOnReturnToMerchant
import net.payrdr.mobile.payment.sdk.screen.fillOutAndSend
import net.payrdr.mobile.payment.sdk.screen.fillOutFormAndSend
import org.junit.Ignore
import org.junit.Test

class PaymentCardPaResUseCaseTest: BaseTestCase() {

    @ScreenShooterTest
    @Test
    fun shouldReturnSuccessPaymentDataWithNewCardPaResWithNoUse3DS2SDKSDK() {
        val mdOrder: String = testOrderHelper.registerOrder()
        val config = CheckoutConfig.MdOrder(mdOrder)
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig)
                SDKPayment.checkout(testActivity, config)
            }
            step("Click on new card button") {
                BottomSheetScreen {
                    clickOnNewCard()
                }
            }
            step("Fill new card form") {
                NewCardScreen {
                    fillOutFormAndSend(cardWith3DSWithPaRes)
                }
            }
            step("Input verification code") {
                ThreeDS1Screen {
                    fillOutAndSend(validVerificationCodePaRes)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe true
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnSuccessPaymentDataWithNewCardPaResWithSessionIdWithNoUse3DS2SDKSDK() {
        val sessionId: String = testOrderHelper.registerSession()
        val config = CheckoutConfig.SessionId(sessionId)
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig)
                SDKPayment.checkout(testActivity, config)
            }
            step("Click on new card button") {
                BottomSheetScreen {
                    clickOnNewCard()
                }
            }
            step("Fill new card form") {
                NewCardScreen {
                    fillOutFormAndSend(cardWith3DSWithPaRes)
                }
            }
            step("Input verification code") {
                ThreeDS1Screen {
                    fillOutAndSend(validVerificationCodePaRes)
                }
            }
            step("Input verification code") {
                ThreeDS1Screen {
                    clickOnReturnToMerchant()
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe sessionId
                    paymentData?.isSuccess shouldBe true
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    @Ignore
    fun shouldReturnErrorPaymentDataWithNewCardPaResWithNoUse3DS2SDKSDKWithInvalidVerificationCode() {
        val mdOrder: String = testOrderHelper.registerOrder()
        val config = CheckoutConfig.MdOrder(mdOrder)
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig)
                SDKPayment.checkout(testActivity, config)
            }
            step("Click on new card button") {
                BottomSheetScreen {
                    clickOnNewCard()
                }
            }
            step("Fill new card form") {
                NewCardScreen {
                    fillOutFormAndSend(cardWith3DSWithPaRes)
                }
            }
            step("Input verification code attempt #1") {
                ThreeDS1Screen {
                    fillOutAndSend(invalidVerificationCodePaRes)
                }
            }
            step("Input verification code attempt #2") {
                Thread.sleep(3_000)
                ThreeDS1Screen {
                    fillOutAndSend(invalidVerificationCodePaRes)
                }
            }
            step("Input verification code attempt #3") {
                Thread.sleep(3_000)
                ThreeDS1Screen {
                    fillOutAndSend(invalidVerificationCodePaRes)
                }
                Thread.sleep(3_000)
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    @Ignore
    fun shouldReturnErrorPaymentDataWithNewCardPaResWithNoUse3DS2SDKSDKWhenUserClickCancel() {
        val mdOrder: String = testOrderHelper.registerOrder()
        val config = CheckoutConfig.MdOrder(mdOrder)
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig)
                SDKPayment.checkout(testActivity, config)
            }
            step("Click on new card button") {
                BottomSheetScreen {
                    clickOnNewCard()
                }
            }
            step("Fill new card form") {
                NewCardScreen {
                    fillOutFormAndSend(cardWith3DSWithPaRes)
                }
            }
            step("Click cancel") {
                ThreeDS1Screen {
                    clickCancel()
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }
}
