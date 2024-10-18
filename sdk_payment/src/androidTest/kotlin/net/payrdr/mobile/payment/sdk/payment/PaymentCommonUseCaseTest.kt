package net.payrdr.mobile.payment.sdk.payment

import com.kaspersky.kaspresso.annotations.ScreenShooterTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import net.payrdr.mobile.payment.sdk.SDKPayment
import net.payrdr.mobile.payment.sdk.core.BaseTestCase
import net.payrdr.mobile.payment.sdk.screen.BottomSheetScreen
import net.payrdr.mobile.payment.sdk.screen.NewCardScreen
import org.junit.Test

class PaymentCommonUseCaseTest : BaseTestCase() {

    @ScreenShooterTest
    @Test
    fun shouldReturnSuccessPaymentForAlreadyPayedOrder() {
        SDKPayment.init(testPaymentConfig)
        val orderId: String = runBlocking {
            testOrderHelper.preparePayedOrder()
        }
        run {
            step("Start checkout") {
                SDKPayment.checkout(testActivity, orderId)
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
    fun shouldReturnErrorPaymentWhenOrderIsNotExist() {
        SDKPayment.init(testPaymentConfig)
        val orderId = "62c828fd-e54e-49e6-b7dd-1ab5e8e2c240"
        run {
            step("Start checkout") {
                SDKPayment.checkout(testActivity, orderId)
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
    fun shouldReturnErrorPaymentExceptionWhenPressBackButtonAtCardForm() {
        SDKPayment.init(testPaymentConfig)
        val orderId: String = runBlocking {
            testOrderHelper.registerOrder()
        }
        run {
            step("Start checkout") {
                SDKPayment.checkout(testActivity, orderId)
            }
            step("Click on new card button") {
                BottomSheetScreen {
                    paymentMethods {
                        firstChild<BottomSheetScreen.NewCard> {
                            isVisible()
                            click()
                        }
                    }
                }
            }
            step("Click back button") {
                NewCardScreen {
                    cardNumberInput {
                        isVisible()
                    }
                    pressBack()
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
}
