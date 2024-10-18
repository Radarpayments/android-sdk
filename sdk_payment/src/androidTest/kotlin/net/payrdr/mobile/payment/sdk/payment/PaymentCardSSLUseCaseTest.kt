package net.payrdr.mobile.payment.sdk.payment

import com.kaspersky.kaspresso.annotations.ScreenShooterTest
import io.kotest.matchers.shouldBe
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.core.BaseTestCase
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.cardSuccessSSL
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.withInvalidCVC
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.withInvalidExpiry
import net.payrdr.mobile.payment.sdk.payment.model.CheckoutConfig
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
    fun shouldReturnSuccessPaymentDataWithNewCardSSLWithUse3DS2SDK() {
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
                    fillOutFormAndSend(cardSuccessSSL)
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
    fun shouldReturnSuccessPaymentDataWithNewCardSSLWithUse3DS2SDKWithSessionId() {
        val sessionId: String = testOrderHelper.registerSession()
        val config = CheckoutConfig.SessionId(sessionId)
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
                    fillOutFormAndSend(cardSuccessSSL)
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
    fun shouldReturnErrorPaymentDataWithNewCardSSLWithUse3DS2SDKWithInvalidCVC() {
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
                    fillOutFormAndSend(cardSuccessSSL.withInvalidCVC())
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
    fun shouldReturnErrorPaymentDataWithNewCardSSLWithUse3DS2SDKWithSessionIdWithInvalidCVC() {
        val sessionId: String = testOrderHelper.registerSession()
        val config = CheckoutConfig.SessionId(sessionId)
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
                    fillOutFormAndSend(cardSuccessSSL.withInvalidCVC())
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
    fun shouldReturnErrorPaymentDataWithNewCardSSLWithUse3DS2SDKWithInvalidExpiry() {
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
                    fillOutFormAndSend(cardSuccessSSL.withInvalidExpiry())
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
    fun shouldReturnErrorPaymentDataWithNewCardSSLWithUse3DS2SDKWithSessionIdWithInvalidExpiry() {
        val sessionId: String = testOrderHelper.registerSession()
        val config = CheckoutConfig.SessionId(sessionId)
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
                    fillOutFormAndSend(cardSuccessSSL.withInvalidExpiry())
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
    fun shouldReturnSuccessPaymentDataWithNewCardSSLWithUse3DS2SDKWithSaveCard() {
        val clientId = testClientIdHelper.getNewTestClientId()
        val mdOrder: String = testOrderHelper.registerOrder(clientId = clientId)
        var secondOrder: String? = null
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
                    fillOutFormAndSend(cardSuccessSSL)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe true
                    resetPaymentData()
                }
            }
            step("Start checkout with saved card") {
                secondOrder = testOrderHelper.registerOrder(clientId = clientId).also {
                    SDKPayment.checkout(testActivity, CheckoutConfig.MdOrder(it))
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
                    paymentData?.sessionId shouldBe secondOrder
                    paymentData?.isSuccess shouldBe true
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentDataWithNewCardSSLWithNoUse3DS2SDKSDK() {
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
                    fillOutFormAndSend(cardSuccessSSL)
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
    fun shouldReturnErrorPaymentDataWithNewCardSSLWithSessionIdWithNoUse3DS2SDKSDK() {
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
                    fillOutFormAndSend(cardSuccessSSL)
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
    fun shouldReturnErrorPaymentDataWithNewCardSSLWithNoUse3DS2SDKSDKWithInvalidCVC() {
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
                    fillOutFormAndSend(cardSuccessSSL.withInvalidCVC())
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
    fun shouldReturnErrorPaymentDataWithNewCardSSLWithNoUse3DS2SDKSDKWithSessionIdWithInvalidCVC() {
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
                    fillOutFormAndSend(cardSuccessSSL.withInvalidCVC())
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
    fun shouldReturnErrorPaymentDataWithNewCardSSLWithNoUse3DS2SDKSDKWithInvalidExpiry() {
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
                    fillOutFormAndSend(cardSuccessSSL.withInvalidCVC())
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
    fun shouldReturnErrorPaymentDataWithNewCardSSLWithNoUse3DS2SDKSDKWithSessionIdWithInvalidExpiry() {
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
                    fillOutFormAndSend(cardSuccessSSL.withInvalidCVC())
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
    fun shouldReturnSuccessPaymentDataWithNewCardSSLWithNoUse3DS2SDKSDKWithSaveCard() {
        val clientId = testClientIdHelper.getNewTestClientId()
        val mdOrder: String = testOrderHelper.registerOrder(clientId = clientId)
        var secondOrder: String? = null
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
                    fillOutFormAndSend(cardSuccessSSL)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe true
                    resetPaymentData()
                }
            }
            step("Start checkout with saved card") {
                secondOrder = testOrderHelper.registerOrder(clientId = clientId).also {
                    SDKPayment.checkout(testActivity, CheckoutConfig.MdOrder(it))
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
                    paymentData?.sessionId shouldBe secondOrder
                    paymentData?.isSuccess shouldBe true
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentDataWithNewCardSSLWithNoUse3DS2SDKSDKWithSaveCardWithInvalidCVC() {
        val clientId = testClientIdHelper.getNewTestClientId()
        val mdOrder: String = testOrderHelper.registerOrder(clientId = clientId)
        var secondOrder: String? = null
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
                    fillOutFormAndSend(cardSuccessSSL)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe true
                    resetPaymentData()
                }
            }
            step("Start checkout with saved card") {
                secondOrder = testOrderHelper.registerOrder(clientId = clientId).also {
                    SDKPayment.checkout(testActivity, CheckoutConfig.MdOrder(it))
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
                    paymentData?.sessionId shouldBe secondOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

}
