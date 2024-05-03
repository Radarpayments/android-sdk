package net.payrdr.mobile.payment.sdk.screen

import net.payrdr.mobile.payment.sdk.data.TestCard
import net.payrdr.mobile.payment.sdk.data.TestCardHelper

internal fun BottomSheetScreen.clickOnNewCard() {
    paymentMethods {
        firstChild<BottomSheetScreen.NewCard> {
            isVisible()
            click()
        }
    }
}

internal fun BottomSheetScreen.clickOnSavedCard(card: TestCard) {
    paymentMethods {
        firstChild<BottomSheetScreen.SavedCard> {
            isVisible()
            cardNumber {
                isVisible()
                hasText(TestCardHelper.getLabelForSavedCard(card))
            }
            expiryDate {
                isVisible()
                hasText(TestCardHelper.getExpireForSavedCard(card))
            }
            click()
        }
    }
}
