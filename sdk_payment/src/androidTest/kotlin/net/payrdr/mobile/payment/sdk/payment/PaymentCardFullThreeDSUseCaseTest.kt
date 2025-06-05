package net.payrdr.mobile.payment.sdk.payment

import com.kaspersky.kaspresso.annotations.ScreenShooterTest
import io.kotest.matchers.shouldBe
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.core.BaseTestCase
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.cardSuccessFull3DS2
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.withInvalidCVC
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.withInvalidExpiry
import net.payrdr.mobile.payment.sdk.payment.model.CheckoutConfig
import net.payrdr.mobile.payment.sdk.screen.BottomSheetScreen
import net.payrdr.mobile.payment.sdk.screen.NewCardScreen
import net.payrdr.mobile.payment.sdk.screen.ThreeDS1Screen
import net.payrdr.mobile.payment.sdk.screen.clickFail
import net.payrdr.mobile.payment.sdk.screen.clickOnNewCard
import net.payrdr.mobile.payment.sdk.screen.clickOnReturnToMerchant
import net.payrdr.mobile.payment.sdk.screen.clickSuccess
import net.payrdr.mobile.payment.sdk.screen.fillOutFormAndSend
import org.junit.Test

@Suppress("LargeClass")
class PaymentCardFullThreeDSUseCaseTest : BaseTestCase() {

    @ScreenShooterTest
    @Test
    fun shouldReturnSuccessPaymentWithNewCardFullThreeDSNoUse3DS2SDKSDK() {
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
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe true
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnSuccessPaymentWithNewCardFullThreeDSNoUse3DS2SDKSDKWithSessionId() {
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
                    fillOutFormAndSend(cardSuccessFull3DS2)
                }
            }
            step("Input verification code") {
                ThreeDS1Screen {
                    clickSuccess()
                }
            }
            step("Return to merchant") {
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
    fun shouldReturnErrorPaymentWithNewCardFullThreeDSNoUse3DS2SDKSDKWithInvalidVerificationCode() {
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
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardFullThreeDSNoUse3DS2SDKSDKWithSessionIdWithInvalidVerificationCode() {
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
                    fillOutFormAndSend(cardSuccessFull3DS2)
                }
            }
            step("Input verification code") {
                ThreeDS1Screen {
                    clickFail()
                }
            }
            step("Return to merchant") {
                ThreeDS1Screen {
                    clickOnReturnToMerchant()
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe sessionId
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardFullThreeDSNoUse3DS2SDKSDKWithInvalidCVC() {
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
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardFullThreeDSNoUse3DS2SDKSDKWithSessionIdWithInvalidCVC() {
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
                    fillOutFormAndSend(cardSuccessFull3DS2.withInvalidCVC())
                }
            }
            step("Input verification code") {
                ThreeDS1Screen {
                    clickSuccess()
                }
            }
            step("Return to merchant") {
                ThreeDS1Screen {
                    clickOnReturnToMerchant()
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe sessionId
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardFullThreeDSNoUse3DS2SDKSDKWithInvalidExpiry() {
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
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardFullThreeDSNoUse3DS2SDKSDKWithSessionIdWithInvalidExpiry() {
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
                    fillOutFormAndSend(cardSuccessFull3DS2.withInvalidExpiry())
                }
            }
            step("Input verification code") {
                ThreeDS1Screen {
                    clickSuccess()
                }
            }
            step("Return to merchant") {
                ThreeDS1Screen {
                    clickOnReturnToMerchant()
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe sessionId
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }
}
