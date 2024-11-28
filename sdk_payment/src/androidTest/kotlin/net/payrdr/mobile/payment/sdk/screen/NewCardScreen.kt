package net.payrdr.mobile.payment.sdk.screen

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.check.KCheckBox
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.text.KButton
import net.payrdr.mobile.payment.sdk.R
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

    val phoneNumberInput = KEditText { withId(R.id.phoneNumberInput) }

    val emailInput = KEditText { withId(R.id.emailInput) }

    val stateInput = KEditText { withId(R.id.stateInput) }

    val countryInput = KEditText { withId(R.id.countryInput) }

    val cityInput = KEditText { withId(R.id.cityInput) }

    val postalCodeInput = KEditText { withId(R.id.postalCodeInput) }

    val addressLine1Input = KEditText { withId(R.id.addressLine1Input) }

    val addressLine2Input = KEditText { withId(R.id.addressLine2Input) }

    val addressLine3Input = KEditText { withId(R.id.addressLine3Input) }

}
