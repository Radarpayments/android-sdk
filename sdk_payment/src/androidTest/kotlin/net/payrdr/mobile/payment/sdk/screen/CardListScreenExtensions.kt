package net.payrdr.mobile.payment.sdk.screen

fun CardListScreen.clickOnEdit() {
    editList {
        isVisible()
        perform { click() }
    }
}

fun CardListScreen.clickOnDeleteIcon() {
    deleteIcon {
        isVisible()
        perform { click() }
    }
}

fun CardListScreen.clickApproveDeleteCard() {
    deleteDialogButton {
        isVisible()
        perform { click() }
    }
}
