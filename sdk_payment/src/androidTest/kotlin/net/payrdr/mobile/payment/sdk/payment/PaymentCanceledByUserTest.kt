package net.payrdr.mobile.payment.sdk.payment

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.kaspersky.kaspresso.annotations.ScreenShooterTest
import io.kotest.matchers.shouldBe
import net.payrdr.mobile.payment.sdk.SDKCanceledByUserException
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.core.BaseTestCase
import net.payrdr.mobile.payment.sdk.data.TestCardHelper.cardSuccessFrictionless3DS2
import net.payrdr.mobile.payment.sdk.payment.model.CheckoutConfig
import net.payrdr.mobile.payment.sdk.screen.BottomSheetScreen
import net.payrdr.mobile.payment.sdk.screen.CardListScreen
import net.payrdr.mobile.payment.sdk.screen.NewCardScreen
import net.payrdr.mobile.payment.sdk.screen.SelectedCardScreen
import net.payrdr.mobile.payment.sdk.screen.clickOnAllPaymentMethods
import net.payrdr.mobile.payment.sdk.screen.clickOnBackButton
import net.payrdr.mobile.payment.sdk.screen.clickOnNewCard
import net.payrdr.mobile.payment.sdk.screen.clickOnSavedCard
import org.junit.Test

class PaymentCanceledByUserTest : BaseTestCase() {

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorWithSDKCanceledByUserExceptionWhenCloseCardNew() {
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
            step("Click on back button in app bar") {
                NewCardScreen {
                    arrowBack.isVisible()
                    arrowBack.perform { click() }
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                    paymentData?.exception shouldBe SDKCanceledByUserException()
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorWithSDKCanceledByUserExceptionWhenClosePaymentBottomSheet() {
        val mdOrder: String = testOrderHelper.registerOrder()
        val config = CheckoutConfig.MdOrder(mdOrder)
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig)
                SDKPayment.checkout(testActivity, config)
            }
            step("Dismiss bottom sheet") {
                BottomSheetScreen {
                    dismissButton.isVisible()
                    dismissButton.perform { click() }
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                    paymentData?.exception shouldBe SDKCanceledByUserException()
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorWithSDKCanceledByUserExceptionWhenCloseCardList() {
        val mdOrder: String = testOrderHelper.registerOrder()
        val config = CheckoutConfig.MdOrder(mdOrder)
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig)
                SDKPayment.checkout(testActivity, config)
            }
            step("Open card list") {
                BottomSheetScreen {
                    clickOnAllPaymentMethods()
                }
            }
            step("Click on back button") {
                CardListScreen {
                    clickOnBackButton()
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                    paymentData?.exception shouldBe SDKCanceledByUserException()
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorWithSDKCanceledByUserExceptionWhenCloseGooglePay() {
        val mdOrder: String = testOrderHelper.registerOrder(clientId = "clientId")
        val config = CheckoutConfig.MdOrder(mdOrder)
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig)
                SDKPayment.checkout(testActivity, config)
            }
            step("Open GPay") {
                BottomSheetScreen {
                    googlePayButton.isVisible()
                    googlePayButton.perform { click() }
                    device.uiDevice.wait(
                        Until.hasObject(By.pkg("com.google.android.gms").depth(0)),
                        10_000
                    )
                }
            }
            step("Close GPay") {
                device.uiDevice.pressBack()
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                    paymentData?.exception shouldBe SDKCanceledByUserException()
                }
            }
        }
    }

    @ScreenShooterTest
    @Test
    fun shouldReturnErrorWithSDKCanceledByUserExceptionWhenCloseSelectedCard() {
        val mdOrder: String = testOrderHelper.registerOrder(clientId = "clientId")
        val config = CheckoutConfig.MdOrder(mdOrder)
        run {
            step("Start checkout") {
                SDKPayment.init(testPaymentConfig)
                SDKPayment.checkout(testActivity, config)
            }
            step("Open selected card") {
                BottomSheetScreen {
                    clickOnSavedCard(cardSuccessFrictionless3DS2)
                }
            }
            step("Press back") {
                SelectedCardScreen {
                    backButtonArrow {
                        isVisible()
                        perform { click() }
                    }
                }
            }
            step("Verify result") {
                verifyResult {
                    paymentData?.sessionId shouldBe mdOrder
                    paymentData?.isSuccess shouldBe false
                    paymentData?.exception shouldBe SDKCanceledByUserException()
                }
            }
        }
    }
}
