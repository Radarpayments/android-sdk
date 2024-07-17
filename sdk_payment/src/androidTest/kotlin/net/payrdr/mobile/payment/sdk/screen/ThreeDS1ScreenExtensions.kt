package net.payrdr.mobile.payment.sdk.screen

import androidx.test.espresso.web.webdriver.Locator

internal fun ThreeDS1Screen.clickSuccess() {
    safeWebElementInteraction {
        webView {
            withElement(
                Locator.XPATH, "/html/body/div/div[2]/div[1]/button[1]"
            ) {
                click()
            }
        }
    }
}

internal fun ThreeDS1Screen.clickFail() {
    safeWebElementInteraction {
        webView {
            withElement(
                Locator.XPATH, "/html/body/div/div[2]/div[1]/button[2]"
            ) {
                click()
            }
        }
    }
}

internal fun ThreeDS1Screen.fillOutAndSend(verificationCode: String) {
    safeWebElementInteraction {
        webView {
            withElement(
                Locator.XPATH, "/html/body/main/form/div/input"
            ) {
                clear()
                keys(verificationCode)
            }
        }
    }
}

internal fun ThreeDS1Screen.clickCancel() {
    safeWebElementInteraction {
        webView {
            withElement(
                Locator.XPATH, "/html/body/main/a"
            ) {
                click()
            }
        }
    }
}

internal fun ThreeDS1Screen.clickOnReturnToMerchant() {
    safeWebElementInteraction {
        webView {
            withElement(
                Locator.XPATH, "/html/body/div[2]/div/main/section[1]/a"
            ) {
                click()
            }
        }
    }
}

internal fun safeWebElementInteraction(action: () -> Unit) {
    var leftAttempt = 10
    while (leftAttempt-- > 0) {
        try {
            action()
            leftAttempt = 0
        } catch (ex: Exception) {
            if (leftAttempt <= 0) {
                throw ex
            } else {
                Thread.sleep(1_000)
            }
        }
    }
}
