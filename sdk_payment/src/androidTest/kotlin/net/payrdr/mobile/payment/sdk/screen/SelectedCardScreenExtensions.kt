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

internal fun SelectedCardScreen.fillOutWithEmailAndPhoneFieldsAndSend(
    card: TestCard,
) {
    cardCodeInput {
        isVisible()
        typeText(card.cvc)
    }
    emailInput {
        isVisible()
        typeText("test@test.com")
    }
    phoneNumberInput {
        isVisible()
        typeText("+35799902871")
    }
    closeSoftKeyboard()
    doneButton {
        scrollTo()
        click()
    }
}
