package net.payrdr.mobile.payment.sdk.payment

import com.kaspersky.kaspresso.annotations.ScreenShooterTest
import io.kotest.matchers.shouldBe
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.core.BaseTestCase
import net.payrdr.mobile.payment.sdk.data.TestAdditionalPayerParams
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.cardSuccessAttempt3DS2
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.invalidVerificationCode
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.validVerificationCode
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.withInvalidCVC
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.withInvalidExpiry
import net.payrdr.mobile.payment.sdk.payment.model.CheckoutConfig
import net.payrdr.mobile.payment.sdk.screen.BottomSheetScreen
import net.payrdr.mobile.payment.sdk.screen.NewCardScreen
import net.payrdr.mobile.payment.sdk.screen.ThreeDS1Screen
import net.payrdr.mobile.payment.sdk.screen.ThreeDS2Screen
import net.payrdr.mobile.payment.sdk.screen.ThreeDSSDKWebViewScreen
import net.payrdr.mobile.payment.sdk.screen.clickFail
import net.payrdr.mobile.payment.sdk.screen.clickOnNewCard
import net.payrdr.mobile.payment.sdk.screen.clickOnReturnToMerchant
import net.payrdr.mobile.payment.sdk.screen.clickSuccess
import net.payrdr.mobile.payment.sdk.screen.fillOutFormAndSend
import net.payrdr.mobile.payment.sdk.screen.inputCode
import net.payrdr.mobile.payment.sdk.screen.onClickVerify
import org.junit.Ignore
import org.junit.Test

@Suppress("LargeClass")
class PaymentCardAttemptThreeDSUseCaseTest : BaseTestCase() {

    @ScreenShooterTest
    @Test
    fun shouldReturnSuccessPaymentWithNewCardAttemptThreeDSUse3DS2SDK() {
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
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe true
                    paymentData?.exception shouldBe null
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnSuccessPaymentWithNewCardAttemptThreeDSUse3DS2SDKWithSessionId() {
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
                    paymentData?.sessionId shouldBe sessionId
                    paymentData?.isSuccess shouldBe true
                    paymentData?.exception shouldBe null
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSUse3DS2SDKWithInvalidVerificationCode() {
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
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSUse3DS2SDKWithWithSessionIdInvalidVerificationCode() {
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
                    paymentData?.sessionId shouldBe sessionId
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSUse3DS2SDKWithInvalidCVC() {
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
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSUse3DS2SDKWithSessionIdWithInvalidCVC() {
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
                    paymentData?.sessionId shouldBe sessionId
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSUse3DS2SDKWithInvalidExpiry() {
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
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSUse3DS2SDKWithSessionIdWithInvalidExpiry() {
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
                    paymentData?.sessionId shouldBe sessionId
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnSuccessPaymentWithNewCardAttemptThreeDSNoUse3DS2SDKSDK() {
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
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe true
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnSuccessPaymentWithNewCardAttemptThreeDSNoUse3DS2SDKSDKWithSessionId() {
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
                    fillOutFormAndSend(cardSuccessAttempt3DS2)
                }
            }
            step("Input verification code") {
                ThreeDS1Screen {
                    clickSuccess()
                }
            }
            step("return to merchant") {
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
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSNoUse3DS2SDKSDKWithInvalidVerificationCode() {
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
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSNoUse3DS2SDKSDKWithSessionIdWithInvalidVerificationCode() {
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
                    fillOutFormAndSend(cardSuccessAttempt3DS2)
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
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSNoUse3DS2SDKSDKWithInvalidCVC() {
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
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSNoUse3DS2SDKSDKWithSessionIdWithInvalidCVC() {
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
                    fillOutFormAndSend(cardSuccessAttempt3DS2.withInvalidCVC())
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
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSNoUse3DS2SDKSDKWithInvalidExpiry() {
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
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorPaymentWithNewCardAttemptThreeDSNoUse3DS2SDKSDKWithSessionIdWithInvalidExpiry() {
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
                    fillOutFormAndSend(cardSuccessAttempt3DS2.withInvalidExpiry())
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
    fun shouldReturnSuccessWithNewCardAttemptThreeDSUse3DS2SDKWithSDKWebView() {
        val mdOrder: String = testOrderHelper.registerOrder(amount = 333)
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
                    fillOutFormAndSend(cardSuccessAttempt3DS2)
                }
            }

            step("Input verification code") {
                ThreeDSSDKWebViewScreen {
                    inputCode(validVerificationCode)
                }
            }

            step("Click on verify button") {
                ThreeDSSDKWebViewScreen {
                    onClickVerify()
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
    @Ignore
    @Test
    fun shouldReturnErrorWithNewCardAttemptThreeDSUse3DS2SDKWithSDKWebViewWithInvalidVerificationCode() {
        val mdOrder: String = testOrderHelper.registerOrder(amount = 333)
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
                    fillOutFormAndSend(cardSuccessAttempt3DS2)
                }
            }

            step("Input invalid verification code") {
                ThreeDSSDKWebViewScreen {
                    inputCode(invalidVerificationCode)
                }
            }

            step("Click on verify button") {
                ThreeDSSDKWebViewScreen {
                    onClickVerify()
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

    @Test
    @ScreenShooterTest
    @Suppress("LongMethod")
    fun shouldReturnSuccessPaymentWithNewCardAttemptWithAdditionalFieldsThreeDSUse3DS2SDK() {
        val mdOrder: String = testOrderHelper.registerOrder(
            testAdditionalPayerParams = TestAdditionalPayerParams(
                billingPayerData = mapOf(
                    "billingCity" to "Berlin",
                    "billingPostalCode" to "123567"
                ),
                email = "test@test.mail.com",
                mobilePhone = null
            )
        )
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
            step("Fill card number with VISA system") {
                NewCardScreen {
                    cardNumberInput {
                        typeText("4000001111111118")
                    }
                    cardExpiryInput {
                        isVisible()
                        typeText("1230")
                    }
                    cardCodeInput {
                        isVisible()
                        typeText("123")
                    }
                }
            }
            step("Additional params should be visible with prefilled values") {
                NewCardScreen {
                    emailInput {
                        isVisible()
                        hasText("test@test.mail.com")
                    }
                    phoneNumberInput {
                        isVisible()
                        typeText("35799902871")
                    }
                    cityInput {
                        isVisible()
                        hasText("Berlin")
                    }
                    countryInput {
                        isVisible()
                        typeText("USA")
                    }
                    stateInput {
                        isVisible()
                        typeText("LA")
                    }
                    postalCodeInput {
                        isVisible()
                        hasText("123567")
                    }
                    doneButton {
                        scrollTo()
                        click()
                    }
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
}
