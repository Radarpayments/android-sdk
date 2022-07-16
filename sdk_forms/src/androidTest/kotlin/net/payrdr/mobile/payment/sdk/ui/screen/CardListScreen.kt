package net.payrdr.mobile.payment.sdk.ui.screen

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.ui.CardListActivity

object CardListScreen : KScreen<CardListScreen>() {
    override val layoutId: Int?
        get() = R.layout.activity_card_list
    override val viewClass: Class<*>?
        get() = CardListActivity::class.java

    val doneButton = KButton { withId(R.id.addNewCardIcon) }

    val editList = KImageView { withId(R.id.editCardsList) }

    val bindingCard3890 = KTextView { withText("** 3890") }

    val bindingCard5675 = KTextView { withText("** 5675") }

    val bindingCard1441 = KTextView { withText("** 1441") }

    val bindingCard0377 = KTextView { withText("** 0377") }

    val bindingCard0115 = KTextView { withText("** 0115") }

    val bindingCard1234 = KTextView { withText("** 1234") }

    val bindingCard6281 = KTextView { withText("** 6281") }
}
