package net.payrdr.mobile.payment.sdk.screen

import net.payrdr.mobile.payment.sdk.data.TestCard

internal fun SelectedCardScreen.fillOutFormAndSend(card: TestCard) {
    cardCodeInput {
        isVisible()
        typeText(card.cvc)
    }
    closeSoftKeyboard()
    doneButton {
        isVisible()
        click()
    }
}
