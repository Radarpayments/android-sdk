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
    if (card.pan.startsWith('4')) {
        phoneNumberInput {
            typeText("+79233487523")
        }
        emailInput {
            typeText("test@test.com")
        }
    }
    doneButton {
        click()
    }
}

internal fun NewCardScreen.fillOutAllAdditionalFieldsAndSend(
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
    phoneNumberInput {
        typeText("+35799902871")
    }
    emailInput {
        typeText("test@test.com")
    }
//    cityInput {
//        typeText("New York City")
//    }
//    countryInput {
//        typeText("USA")
//    }
//    stateInput {
//        typeText("New York")
//    }
//    postalCodeInput {
//        typeText("123600")
//    }
//    addressLine1Input {
//        typeText("Address 1")
//    }
//    addressLine2Input {
//        typeText("Address 2")
//    }
//    addressLine3Input {
//        typeText("Address 3")
//    }
    doneButton {
        scrollTo()
        click()
    }
}

