package net.payrdr.mobile.payment.sdk.payment

import com.kaspersky.kaspresso.annotations.ScreenShooterTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.core.BaseTestCase
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.invalidVerificationCode
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.cardSuccessFull3DS2
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

class PaymentCardFullThreeDSUseCaseTest : BaseTestCase() {

    @ScreenShooterTest
    @Test
    fun shouldReturnSuccessPaymentWithNewCardFullThreeDSUse3DS2SDK() {
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
                    fillOutFormAndSend(cardSuccessFull3DS2)
                }
            }
            step("Input verification code") {
                ThreeDS2Screen {
                    fillOutFormAndSend(validVerificationCode)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.isSuccess shouldBe true
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardFullThreeDSUse3DS2SDKWithInvalidVerificationCode() {
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
                    fillOutFormAndSend(cardSuccessFull3DS2)
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
                    paymentData?.isSuccess shouldBe false
                    paymentData?.exception shouldNotBe null
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardFullThreeDSUse3DS2SDKWithInvalidCVC() {
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
                    fillOutFormAndSend(cardSuccessFull3DS2.withInvalidCVC())
                }
            }
            step("Input verification code") {
                ThreeDS2Screen {
                    fillOutFormAndSend(validVerificationCode)
                }

            }
            step("Verify result") {
                verifyResult {
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardFullThreeDSUse3DS2SDKWithInvalidExpiry() {
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
                    fillOutFormAndSend(cardSuccessFull3DS2.withInvalidExpiry())
                }
            }
            step("Input verification code") {
                ThreeDS2Screen {
                    fillOutFormAndSend(validVerificationCode)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnSuccessPaymentWithNewCardFullThreeDSNoUse3DS2SDKSDK() {
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
                    fillOutFormAndSend(cardSuccessFull3DS2)
                }
            }
            step("Input verification code") {
                ThreeDS1Screen {
                    clickSuccess()
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.isSuccess shouldBe true
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardFullThreeDSNoUse3DS2SDKSDKWithInvalidVerificationCode() {
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
                    fillOutFormAndSend(cardSuccessFull3DS2)
                }
            }
            step("Input verification code") {
                ThreeDS1Screen {
                    clickFail()
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardFullThreeDSNoUse3DS2SDKSDKWithInvalidCVC() {
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
                    fillOutFormAndSend(cardSuccessFull3DS2.withInvalidCVC())
                }
            }
            step("Input verification code") {
                ThreeDS1Screen {
                    clickSuccess()
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardFullThreeDSNoUse3DS2SDKSDKWithInvalidExpiry() {
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
                    fillOutFormAndSend(cardSuccessFull3DS2.withInvalidExpiry())
                }
            }
            step("Input verification code") {
                ThreeDS1Screen {
                    clickSuccess()
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

}
