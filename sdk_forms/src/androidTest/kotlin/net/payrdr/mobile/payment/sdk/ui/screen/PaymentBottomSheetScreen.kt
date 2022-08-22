package net.payrdr.mobile.payment.sdk.ui.screen

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.ui.PaymentBottomSheetFragment

object PaymentBottomSheetScreen : KScreen<PaymentBottomSheetScreen>() {
    override val layoutId: Int?
        get() = R.layout.fragment_bottom_sheet_payment
    override val viewClass: Class<*>?
        get() = PaymentBottomSheetFragment::class.java

    val dismissButton = KButton { withId(R.id.dismissButton) }.also {
        it.inRoot { isDialog() }
    }

    val bindingCard7724 = KTextView { withText("** 7724") }.also {
        it.inRoot { isDialog() }
    }

    val bindingCard6614 = KTextView { withText("** 6614") }.also {
        it.inRoot { isDialog() }
    }

    val bindingCard0000 = KTextView { withText("** 0000") }.also {
        it.inRoot { isDialog() }
    }

    val bindingCard3456 = KTextView { withText("** 3456") }.also {
        it.inRoot { isDialog() }
    }

    val addNewCardText = KTextView { withId(R.id.newCardText) }.also {
        it.inRoot { isDialog() }
    }

    val allPaymentMethods = KTextView { withId(R.id.allPaymentMethod) }.also {
        it.inRoot { isDialog() }
    }
}
