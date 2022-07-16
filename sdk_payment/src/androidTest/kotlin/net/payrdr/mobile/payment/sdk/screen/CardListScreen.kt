package net.payrdr.mobile.payment.sdk.screen

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import net.payrdr.mobile.payment.sdk.R
import net.payrdr.mobile.payment.sdk.form.R.id.addNewCardIcon
import net.payrdr.mobile.payment.sdk.form.ui.CardListActivity

object CardListScreen : KScreen<CardListScreen>() {
    override val layoutId: Int?
        get() = R.layout.activity_card_list
    override val viewClass: Class<*>?
        get() = CardListActivity::class.java

    val doneButton = KButton { withId(addNewCardIcon) }

    val editList = KImageView { withId(R.id.editCardsList) }

    val bindingCard = KTextView { withText("** 1118") }
}
