package net.payrdr.mobile.payment.sdk.screen

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import net.payrdr.mobile.payment.sdk.R
import net.payrdr.mobile.payment.sdk.form.ui.PaymentBottomSheetFragment

object BottomSheetScreen : KScreen<BottomSheetScreen>() {
    override val layoutId: Int?
        get() = R.layout.fragment_bottom_sheet_payment
    override val viewClass: Class<*>?
        get() = PaymentBottomSheetFragment::class.java

    val newCardItem = KButton { withId(R.id.newCardItem) }.also {
        it.inRoot { isDialog() }
    }

    val allPayment = KButton { withId(R.id.allPaymentMethodLayout) }.also {
        it.inRoot { isDialog() }
    }

    val addNewCard = KTextView { withId(R.id.newCardText) }.also {
        it.inRoot { isDialog() }
    }
}
