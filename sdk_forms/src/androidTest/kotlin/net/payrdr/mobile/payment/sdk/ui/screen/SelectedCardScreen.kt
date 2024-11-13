package net.payrdr.mobile.payment.sdk.ui.screen

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.text.KButton
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.ui.CardSelectedActivity

object SelectedCardScreen : KScreen<SelectedCardScreen>() {
    override val layoutId: Int?
        get() = R.layout.activity_card_selected
    override val viewClass: Class<*>?
        get() = CardSelectedActivity::class.java

    val doneButton = KButton { withId(R.id.doneButton) }
    val cardCodeInput = KEditText { withId(R.id.cardCodeInput) }
    val cvcIncorrectText = KView { withText(R.string.payrdr_card_incorrect_cvc) }
    val cardCodeInputLayout = KTextInputLayout { withId(R.id.cardCodeInputLayout) }
}
