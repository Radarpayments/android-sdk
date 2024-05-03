package net.payrdr.mobile.payment.sdk.payment

import com.kaspersky.kaspresso.annotations.ScreenShooterTest
import io.kotest.matchers.shouldBe
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.core.BaseTestCase
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.invalidVerificationCode
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.cardSuccessAttempt3DS2
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.validVerificationCode
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.withInvalidCVC
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.withInvalidExpiry
import net.payrdr.mobile.payment.sdk.screen.BottomSheetScreen
import net.payrdr.mobile.payment.sdk.screen.NewCardScreen
import net.payrdr.mobile.payment.sdk.screen.ThreeDS1Screen
import net.payrdr.mobile.payment.sdk.screen.ThreeDS2Screen
import net.payrdr.mobile.payment.sdk.screen.clickFail
import net.payrdr.mobile.payment.sdk.screen.clickOnNewCard
import net.payrdr.mobile.payment.sdk.screen.clickSuccess
import net.payrdr.mobile.payment.sdk.screen.fillOutFormAndSend
import org.junit.Test

class PaymentCardAttemptThreeDSUseCaseTest : BaseTestCase() {

    @ScreenShooterTest
    @Test
    fun shouldReturnSuccessPaymentWithNewCardAttemptThreeDSUse3DS2() {
        val mdOrder: String = testOrderHelper.registerOrder()
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig.copy(use3DSConfig = testConfigForUse3DS2))
                SDKPayment.checkout(testActivity, mdOrder)
            }
            step("Click on new card button") {
                BottomSheetScreen {
                    clickOnNewCard()
                }
            }
            step("Fill new card form") {
                NewCardScreen {
                    fillOutFormAndSend(cardSuccessAttempt3DS2)
                }
            }
            step("Input verification code") {
                ThreeDS2Screen {
                    fillOutFormAndSend(validVerificationCode)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.mdOrder shouldBe mdOrder
                    paymentData?.isSuccess shouldBe true
                    paymentData?.exception shouldBe null
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSUse3DS2WithInvalidVerificationCode() {
        val mdOrder: String = testOrderHelper.registerOrder()
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig.copy(use3DSConfig = testConfigForUse3DS2))
                SDKPayment.checkout(testActivity, mdOrder)
            }
            step("Click on new card button") {
                BottomSheetScreen {
                    clickOnNewCard()
                }
            }
            step("Fill new card form") {
                NewCardScreen {
                    fillOutFormAndSend(cardSuccessAttempt3DS2)
                }
            }
            step("Input verification code attempt #1") {
                ThreeDS2Screen {
                    fillOutFormAndSend(invalidVerificationCode)
                }
            }
            step("Input verification code attempt #2") {
                ThreeDS2Screen {
                    fillOutFormAndSend(invalidVerificationCode)
                }
            }
            step("Input verification code attempt #3") {
                ThreeDS2Screen {
                    fillOutFormAndSend(invalidVerificationCode)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.mdOrder shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSUse3DS2WithInvalidCVC() {
        val mdOrder: String = testOrderHelper.registerOrder()
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig.copy(use3DSConfig = testConfigForUse3DS2))
                SDKPayment.checkout(testActivity, mdOrder)
            }
            step("Click on new card button") {
                BottomSheetScreen {
                    clickOnNewCard()
                }
            }
            step("Fill new card form") {
                NewCardScreen {
                    fillOutFormAndSend(cardSuccessAttempt3DS2.withInvalidCVC())
                }
            }
            step("Input verification code") {
                ThreeDS2Screen {
                    fillOutFormAndSend(validVerificationCode)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.mdOrder shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSUse3DS2WithInvalidExpiry() {
        val mdOrder: String = testOrderHelper.registerOrder()
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig.copy(use3DSConfig = testConfigForUse3DS2))
                SDKPayment.checkout(testActivity, mdOrder)
            }
            step("Click on new card button") {
                BottomSheetScreen {
                    clickOnNewCard()
                }
            }
            step("Fill new card form") {
                NewCardScreen {
                    fillOutFormAndSend(cardSuccessAttempt3DS2.withInvalidExpiry())
                }
            }
            step("Input verification code") {
                ThreeDS2Screen {
                    fillOutFormAndSend(validVerificationCode)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.mdOrder shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnSuccessPaymentWithNewCardAttemptThreeDSUse3DS1() {
        val mdOrder: String = testOrderHelper.registerOrder()
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig)
                SDKPayment.checkout(testActivity, mdOrder)
            }
            step("Click on new card button") {
                BottomSheetScreen {
                    clickOnNewCard()
                }
            }
            step("Fill new card form") {
                NewCardScreen {
                    fillOutFormAndSend(cardSuccessAttempt3DS2)
                }
            }
            step("Input verification code") {
                ThreeDS1Screen {
                    clickSuccess()
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.mdOrder shouldBe mdOrder
                    paymentData?.isSuccess shouldBe true
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSUse3DS1WithInvalidVerificationCode() {
        val mdOrder: String = testOrderHelper.registerOrder()
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig)
                SDKPayment.checkout(testActivity, mdOrder)
            }
            step("Click on new card button") {
                BottomSheetScreen {
                    clickOnNewCard()
                }
            }
            step("Fill new card form") {
                NewCardScreen {
                    fillOutFormAndSend(cardSuccessAttempt3DS2)
                }
            }
            step("Input verification code") {
                ThreeDS1Screen {
                    clickFail()
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.mdOrder shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSUse3DS1WithInvalidCVC() {
        val mdOrder: String = testOrderHelper.registerOrder()
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig)
                SDKPayment.checkout(testActivity, mdOrder)
            }
            step("Click on new card button") {
                BottomSheetScreen {
                    clickOnNewCard()
                }
            }
            step("Fill new card form") {
                NewCardScreen {
                    fillOutFormAndSend(cardSuccessAttempt3DS2.withInvalidCVC())
                }
            }
            step("Input verification code") {
                ThreeDS1Screen {
                    clickSuccess()
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.mdOrder shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSUse3DS1WithInvalidExpiry() {
        val mdOrder: String = testOrderHelper.registerOrder()
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig)
                SDKPayment.checkout(testActivity, mdOrder)
            }
            step("Click on new card button") {
                BottomSheetScreen {
                    clickOnNewCard()
                }
            }
            step("Fill new card form") {
                NewCardScreen {
                    fillOutFormAndSend(cardSuccessAttempt3DS2.withInvalidExpiry())
                }
            }
            step("Input verification code") {
                ThreeDS1Screen {
                    clickSuccess()
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.mdOrder shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

}
