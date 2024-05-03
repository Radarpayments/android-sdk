package net.payrdr.mobile.payment.sdk.payment

import com.kaspersky.kaspresso.annotations.ScreenShooterTest
import io.kotest.matchers.shouldBe
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.core.BaseTestCase
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.cardSuccessSSL
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.withInvalidCVC
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.withInvalidExpiry
import net.payrdr.mobile.payment.sdk.screen.BottomSheetScreen
import net.payrdr.mobile.payment.sdk.screen.NewCardScreen
import net.payrdr.mobile.payment.sdk.screen.SelectedCardScreen
import net.payrdr.mobile.payment.sdk.screen.clickOnSavedCard
import net.payrdr.mobile.payment.sdk.screen.clickOnNewCard
import net.payrdr.mobile.payment.sdk.screen.fillOutFormAndSend
import org.junit.Test

class PaymentCardSSLUseCaseTest: BaseTestCase() {

    @ScreenShooterTest
    @Test
    fun shouldReturnSuccessPaymentDataWithNewCardSSLWithUse3DS2() {
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
                    fillOutFormAndSend(cardSuccessSSL)
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
    fun shouldReturnErrorPaymentDataWithNewCardSSLWithUse3DS2WithInvalidCVC() {
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
                    fillOutFormAndSend(cardSuccessSSL.withInvalidCVC())
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
    fun shouldReturnErrorPaymentDataWithNewCardSSLWithUse3DS2WithInvalidExpiry() {
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
                    fillOutFormAndSend(cardSuccessSSL.withInvalidExpiry())
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
    fun shouldReturnSuccessPaymentDataWithNewCardSSLWithUse3DS2WithSaveCard() {
        val clientId = testClientIdHelper.getNewTestClientId()
        val mdOrder: String = testOrderHelper.registerOrder(clientId = clientId)
        var secondOrder: String? = null
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
                    fillOutFormAndSend(cardSuccessSSL)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.mdOrder shouldBe mdOrder
                    paymentData?.isSuccess shouldBe true
                    resetPaymentData()
                }
            }
            step("Start checkout with saved card") {
                secondOrder = testOrderHelper.registerOrder(clientId = clientId).also {
                    SDKPayment.checkout(testActivity, it)
                }
            }
            step("Click on saved card item") {
                BottomSheetScreen {
                    clickOnSavedCard(cardSuccessSSL)
                }
            }
            step("Input cvc") {
                SelectedCardScreen {
                    fillOutFormAndSend(cardSuccessSSL)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.mdOrder shouldBe secondOrder
                    paymentData?.isSuccess shouldBe true
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentDataWithNewCardSSLWithUse3DS1() {
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
                    fillOutFormAndSend(cardSuccessSSL)
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
    fun shouldReturnErrorPaymentDataWithNewCardSSLWithUse3DS1WithInvalidCVC() {
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
                    fillOutFormAndSend(cardSuccessSSL.withInvalidCVC())
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
    fun shouldReturnErrorPaymentDataWithNewCardSSLWithUse3DS1WithInvalidExpiry() {
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
                    fillOutFormAndSend(cardSuccessSSL.withInvalidCVC())
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
    fun shouldReturnSuccessPaymentDataWithNewCardSSLWithUse3DS1WithSaveCard() {
        val clientId = testClientIdHelper.getNewTestClientId()
        val mdOrder: String = testOrderHelper.registerOrder(clientId = clientId)
        var secondOrder: String? = null
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
                    fillOutFormAndSend(cardSuccessSSL)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.mdOrder shouldBe mdOrder
                    paymentData?.isSuccess shouldBe true
                    resetPaymentData()
                }
            }
            step("Start checkout with saved card") {
                secondOrder = testOrderHelper.registerOrder(clientId = clientId).also {
                    SDKPayment.checkout(testActivity, it)
                }
            }
            step("Click on saved card item") {
                BottomSheetScreen {
                    clickOnSavedCard(cardSuccessSSL)
                }
            }
            step("Input cvc") {
                SelectedCardScreen {
                    fillOutFormAndSend(cardSuccessSSL)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.mdOrder shouldBe secondOrder
                    paymentData?.isSuccess shouldBe true
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentDataWithNewCardSSLWithUse3DS1WithSaveCardWithInvalidCVC() {
        val clientId = testClientIdHelper.getNewTestClientId()
        val mdOrder: String = testOrderHelper.registerOrder(clientId = clientId)
        var secondOrder: String? = null
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
                    fillOutFormAndSend(cardSuccessSSL)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.mdOrder shouldBe mdOrder
                    paymentData?.isSuccess shouldBe true
                    resetPaymentData()
                }
            }
            step("Start checkout with saved card") {
                secondOrder = testOrderHelper.registerOrder(clientId = clientId).also {
                    SDKPayment.checkout(testActivity, it)
                }
            }
            step("Click on saved card item") {
                BottomSheetScreen {
                    clickOnSavedCard(cardSuccessSSL)
                }
            }
            step("Input cvc") {
                SelectedCardScreen {
                    fillOutFormAndSend(cardSuccessSSL.withInvalidCVC())
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.mdOrder shouldBe secondOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

}
