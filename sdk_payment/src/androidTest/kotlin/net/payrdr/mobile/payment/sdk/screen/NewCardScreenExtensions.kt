package net.payrdr.mobile.payment.sdk.screen

import net.payrdr.mobile.payment.sdk.data.TestCard

internal fun NewCardScreen.fillOutFormAndSend(
    card: TestCard,
) {
    cardNumberInput {
        isVisible()
        typeText(card.pan)
    }
    cardExpiryInput {
        isVisible()
        typeText(card.expiry)
    }
    cardCodeInput {
        isVisible()
        typeText(card.cvc)
    }
    doneButton {
        click()
    }
}
