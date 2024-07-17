package net.payrdr.mobile.payment.sdk.screen

import androidx.test.espresso.web.webdriver.Locator

internal fun ThreeDSSDKWebViewScreen.inputCode(verificationCode: String ) {
    safeWebElementInteraction {
        webView {
            this.view.forceJavascriptEnabled()
            withElement(
                Locator.XPATH, "/html/body/span/form/input"
            ) {
                click()
                clear()
                keys(verificationCode)
            }
        }
    }
}

internal fun ThreeDSSDKWebViewScreen.onClickVerify() {
    safeWebElementInteraction {
        webView {
            this.view.forceJavascriptEnabled()
            withElement(
                Locator.XPATH, "/html/body/span/form/button[1]"
            ) {
                click()
            }
        }
    }
}
