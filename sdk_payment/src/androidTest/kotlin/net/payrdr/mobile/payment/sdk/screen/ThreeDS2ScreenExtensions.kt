package net.payrdr.mobile.payment.sdk.screen

internal fun ThreeDS2Screen.fillOutFormAndSend(verificationCode: String) {
    ThreeDS2Screen {
        dataEntry {
            isVisible()
            typeText(verificationCode)
        }
        closeSoftKeyboard()
        Thread.sleep(1_000)
        submit {
            isVisible()
            Thread.sleep(1_000)
            click()
        }
    }
}
