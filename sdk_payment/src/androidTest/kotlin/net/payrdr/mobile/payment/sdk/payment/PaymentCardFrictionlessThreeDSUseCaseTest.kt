package net.payrdr.mobile.payment.sdk.payment

import com.kaspersky.kaspresso.annotations.ScreenShooterTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.core.BaseTestCase
import net.payrdr.mobile.payment.sdk.data.TestAdditionalPayerParams
import net.payrdr.mobile.payment.sdk.data.TestCardHelper
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.cardSuccessFrictionless3DS2
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.getLabelForSavedBindingItem
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.validVerificationCode
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.withInvalidCVC
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.withInvalidExpiry
import net.payrdr.mobile.payment.sdk.payment.model.CheckoutConfig
import net.payrdr.mobile.payment.sdk.screen.BottomSheetScreen
import net.payrdr.mobile.payment.sdk.screen.CardListScreen
import net.payrdr.mobile.payment.sdk.screen.NewCardScreen
import net.payrdr.mobile.payment.sdk.screen.ThreeDS1Screen
import net.payrdr.mobile.payment.sdk.screen.ThreeDS2Screen
import net.payrdr.mobile.payment.sdk.screen.clickApproveDeleteCard
import net.payrdr.mobile.payment.sdk.screen.clickOnAllPaymentMethods
import net.payrdr.mobile.payment.sdk.screen.clickOnDeleteIcon
import net.payrdr.mobile.payment.sdk.screen.clickOnEdit
import net.payrdr.mobile.payment.sdk.screen.clickOnNewCard
import net.payrdr.mobile.payment.sdk.screen.clickOnReturnToMerchant
import net.payrdr.mobile.payment.sdk.screen.fillOutAllAdditionalFieldsAndSend
import net.payrdr.mobile.payment.sdk.screen.fillOutFormAndSend
import org.junit.Test

@Suppress("LargeClass")
class PaymentCardFrictionlessThreeDSUseCaseTest : BaseTestCase() {

    @ScreenShooterTest
    @Test
    fun shouldReturnSuccessPaymentWithNewCardFrictionlessThreeDSUse3DS2SDK() {
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
                    fillOutFormAndSend(cardSuccessFrictionless3DS2)
                }
            }
            step("Input verification code") {
                ThreeDS2Screen {
                    fillOutFormAndSend(validVerificationCode)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe true
                    paymentData?.exception shouldBe null
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnSuccessPaymentWithNewCardFrictionlessThreeDSUse3DS2SDKWithSessionId() {
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
                    fillOutFormAndSend(cardSuccessFrictionless3DS2)
                }
            }
            step("Input verification code") {
                ThreeDS2Screen {
                    fillOutFormAndSend(validVerificationCode)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe sessionId
                    paymentData?.isSuccess shouldBe true
                    paymentData?.exception shouldBe null
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardFrictionlessThreeDSUse3DS2SDKWithInvalidCVC() {
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
                    fillOutFormAndSend(cardSuccessFrictionless3DS2.withInvalidCVC())
                }
            }
            step("Input verification code") {
                ThreeDS2Screen {
                    fillOutFormAndSend(validVerificationCode)
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
    fun shouldReturnErrorPaymentWithNewCardFrictionlessThreeDSUse3DS2SDKWithSessionIdWithInvalidCVC() {
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
                    fillOutFormAndSend(cardSuccessFrictionless3DS2.withInvalidCVC())
                }
            }
            step("Input verification code") {
                ThreeDS2Screen {
                    fillOutFormAndSend(validVerificationCode)
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
    fun shouldReturnErrorPaymentWithNewCardFrictionlessThreeDSUse3DS2SDKWithInvalidExpiry() {
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
                    fillOutFormAndSend(cardSuccessFrictionless3DS2.withInvalidExpiry())
                }
            }
            step("Input verification code") {
                ThreeDS2Screen {
                    fillOutFormAndSend(validVerificationCode)
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
    fun shouldReturnErrorPaymentWithNewCardFrictionlessThreeDSUse3DS2SDKWithSessionIdWithInvalidExpiry() {
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
                    fillOutFormAndSend(cardSuccessFrictionless3DS2.withInvalidExpiry())
                }
            }
            step("Input verification code") {
                ThreeDS2Screen {
                    fillOutFormAndSend(validVerificationCode)
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
    fun shouldReturnSuccessPaymentWithNewCardFrictionlessThreeDSNoUse3DS2SDKSDK() {
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
                    fillOutFormAndSend(cardSuccessFrictionless3DS2)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe true
                    paymentData?.exception shouldBe null
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnSuccessPaymentWithNewCardFrictionlessThreeDSNoUseWithSessionId3DS2SDKSDK() {
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
                    fillOutFormAndSend(cardSuccessFrictionless3DS2)
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
                    paymentData?.exception shouldBe null
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardFrictionlessThreeDSNoUse3DS2SDKSDKWithInvalidCVC() {
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
                    fillOutFormAndSend(cardSuccessFrictionless3DS2.withInvalidCVC())
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
    fun shouldReturnErrorPaymentWithNewCardFrictionlessThreeDSNoUse3DS2SDKSDKWithSessionIdWithInvalidCVC() {
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
                    fillOutFormAndSend(cardSuccessFrictionless3DS2.withInvalidCVC())
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
    fun shouldReturnErrorPaymentWithNewCardFrictionlessThreeDSNoUse3DS2SDKSDKWithInvalidExpiry() {
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
                    fillOutFormAndSend(cardSuccessFrictionless3DS2.withInvalidExpiry())
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
    fun shouldReturnErrorPaymentWithNewCardFrictionlessThreeDSNoUse3DS2SDKSDKWithSessionIdWithInvalidExpiry() {
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
                    fillOutFormAndSend(cardSuccessFrictionless3DS2.withInvalidExpiry())
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
    fun shouldReturnErrorPaymentWithNewCardFrictionlessFailThreeDSUse3DS2SDK() {
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
                    fillOutFormAndSend(TestCardHelper.cardFailFrictionless3DS2)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                    paymentData?.exception shouldNotBe null
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardFrictionlessFailWithSessionIdThreeDSUse3DS2SDK() {
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
                    fillOutFormAndSend(TestCardHelper.cardFailFrictionless3DS2)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe sessionId
                    paymentData?.isSuccess shouldBe false
                    paymentData?.exception shouldNotBe null
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardFrictionlessFailThreeDSNoUse3DS2SDKSDK() {
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
                    fillOutFormAndSend(TestCardHelper.cardFailFrictionless3DS2)
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
    fun shouldReturnErrorPaymentWithNewCardFrictionlessFailThreeDSNoUse3DS2SDKSDKWithSessionId() {
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
                    fillOutFormAndSend(TestCardHelper.cardFailFrictionless3DS2)
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
    fun shouldReturnErrorPaymentWithAdditionalFieldsWithNewCardFrictionlessThreeDSNoUse3DS2SDKSDKWithInvalidExpiry() {
        val mdOrder: String = testOrderHelper.registerOrder(
            testAdditionalPayerParams = TestAdditionalPayerParams(
                billingPayerData = emptyMap(),
                email = null,
                mobilePhone = null
            )
        )
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
                    fillOutAllAdditionalFieldsAndSend(cardSuccessFrictionless3DS2.withInvalidExpiry())
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
    fun shouldSaveNewCardFrictionlessThreeDSNoUse3DS2SDKSDK() {
        val clientId = testClientIdHelper.getNewTestClientId()
        val mdOrder: String = testOrderHelper.registerOrder(clientId = clientId)
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
                    fillOutFormAndSend(cardSuccessFrictionless3DS2)
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.isSuccess shouldBe true
                }
                flakySafely {
                    runBlocking {
                        val mdOrderBinding: String =
                            testOrderHelper.registerOrder(clientId = clientId)
                        val sessionStatus = testOrderHelper.getSessionStatus(mdOrderBinding)
                        sessionStatus.bindingItems?.first()?.label shouldBe getLabelForSavedBindingItem(
                            cardSuccessFrictionless3DS2
                        )
                    }
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldSaveNewCardFrictionlessThreeDSUse3DS2SDK() {
        val clientId = testClientIdHelper.getNewTestClientId()
        val mdOrder: String = testOrderHelper.registerOrder(clientId = clientId)
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
                    fillOutFormAndSend(cardSuccessFrictionless3DS2)
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
                flakySafely {
                    runBlocking {
                        val mdOrderBinding: String =
                            testOrderHelper.registerOrder(clientId = clientId)
                        val sessionStatus = testOrderHelper.getSessionStatus(mdOrderBinding)
                        sessionStatus.bindingItems?.first()?.label shouldBe getLabelForSavedBindingItem(
                            cardSuccessFrictionless3DS2
                        )
                    }
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    @Suppress("LongMethod")
    fun shouldDeleteSavedCardFrictionlessThreeDSNoUse3DS2SDKSDK() {
        val clientId = testClientIdHelper.getNewTestClientId()
        val mdOrder: String = testOrderHelper.registerOrder(clientId = clientId)
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
                    fillOutFormAndSend(cardSuccessFrictionless3DS2)
                }
            }
            step("Verify card saved") {
                val mdOrderBinding: String =
                    testOrderHelper.registerOrder(clientId = clientId)
                verifyResult {
                    paymentData?.isSuccess shouldBe true
                }
                flakySafely {
                    runBlocking {
                        val sessionStatus = testOrderHelper.getSessionStatus(mdOrderBinding)
                        sessionStatus.bindingItems?.first()?.label shouldBe getLabelForSavedBindingItem(
                            cardSuccessFrictionless3DS2
                        )
                        delay(2000)
                        SDKPayment.checkout(testActivity, CheckoutConfig.MdOrder(mdOrderBinding))
                    }
                }
            }
            step("choose all payment methods") {
                BottomSheetScreen {
                    clickOnAllPaymentMethods()
                }
            }
            step("delete saved card") {
                CardListScreen {
                    clickOnEdit()
                    clickOnDeleteIcon()
                    clickApproveDeleteCard()
                    clickOnEdit()
                }
            }
            step("check no saved card") {
                flakySafely {
                    runBlocking {
                        val mdOrderBinding: String =
                            testOrderHelper.registerOrder(clientId = clientId)
                        val sessionStatus = testOrderHelper.getSessionStatus(mdOrderBinding)
                        sessionStatus.bindingItems shouldBe emptyList()
                    }
                }
            }
        }
    }
}
