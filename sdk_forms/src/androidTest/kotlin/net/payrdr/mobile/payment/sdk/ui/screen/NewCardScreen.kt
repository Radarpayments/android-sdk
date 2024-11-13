package net.payrdr.mobile.payment.sdk.ui.screen

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.check.KCheckBox
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.text.KButton
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.ui.CardNewActivity

object NewCardScreen : KScreen<NewCardScreen>() {
    override val layoutId: Int?
        get() = R.layout.activity_card_new
    override val viewClass: Class<*>?
        get() = CardNewActivity::class.java

    val cardNumberInput = KEditText { withId(R.id.cardNumberInput) }

    val cardExpiryInput = KEditText { withId(R.id.cardExpiryInput) }

    val cardCodeInput = KEditText { withId(R.id.cardCodeInput) }

    val cardHolderInput = KEditText { withId(R.id.cardHolderInput) }

    val doneButton = KButton { withId(R.id.doneButton) }

    val checkSaveCard = KCheckBox { withId(R.id.switchBox) }

    val cardNumberInputLayout = KTextInputLayout { withId(R.id.cardNumberInputLayout) }

    val cardCodeInputLayout = KTextInputLayout { withId(R.id.cardCodeInputLayout) }

    val cardExpiryInputLayout = KTextInputLayout { withId(R.id.cardExpiryInputLayout) }
}
