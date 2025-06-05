package net.payrdr.mobile.payment.sdk.screen

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.text.KButton
import net.payrdr.mobile.payment.sdk.R
import net.payrdr.mobile.payment.sdk.form.ui.CardSelectedActivity

object SelectedCardScreen : KScreen<SelectedCardScreen>() {
    override val layoutId: Int?
        get() = R.layout.activity_card_selected
    override val viewClass: Class<*>?
        get() = CardSelectedActivity::class.java

    val cardCodeInput = KEditText { withId(R.id.cardCodeInput) }

    val phoneNumberInput = KEditText { withId(R.id.phoneNumberInput) }

    val emailInput = KEditText { withId(R.id.emailInput) }

    val doneButton = KButton { withId(R.id.doneButton) }
}
