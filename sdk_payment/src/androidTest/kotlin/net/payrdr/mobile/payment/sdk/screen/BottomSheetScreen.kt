package net.payrdr.mobile.payment.sdk.screen

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import net.payrdr.mobile.payment.sdk.R
import net.payrdr.mobile.payment.sdk.form.ui.PaymentBottomSheetFragment
import org.hamcrest.Matcher

object BottomSheetScreen : KScreen<BottomSheetScreen>() {
    override val layoutId: Int?
        get() = R.layout.fragment_bottom_sheet_payment
    override val viewClass: Class<*>?
        get() = PaymentBottomSheetFragment::class.java

    val newCardItem = KButton { withId(R.id.newCardItem) }.also {
        it.inRoot { isDialog() }
    }

    val allPaymentMethods = KView { withId(R.id.allPaymentMethodLayout) }

    val paymentMethods = KRecyclerView(
        builder = { withId(R.id.cardList) },
        itemTypeBuilder = {
            itemType(::NewCard)
            itemType(::SavedCard)
        }
    )

    internal class NewCard(parent: Matcher<View>) : KRecyclerItem<NewCard>(parent)

    internal class SavedCard(parent: Matcher<View>) : KRecyclerItem<SavedCard>(parent) {
        val cardNumber: KTextView = KTextView(parent) { withId(R.id.cardNumber) }
        val expiryDate: KTextView = KTextView(parent) { withId(R.id.expiryDate) }
    }

}
