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

    val phoneNumberInputLayout = KTextInputLayout { withId(R.id.phoneNumberInputLayout) }
    val phoneNumberInput = KEditText { withId(R.id.phoneNumberInput) }

    val emailInputLayout = KTextInputLayout { withId(R.id.emailInputLayout) }
    val emailInput = KEditText { withId(R.id.emailInput) }

    val cityInputLayout = KTextInputLayout { withId(R.id.cityInputLayout) }
    val cityInput = KEditText { withId(R.id.cityInput) }

    val countryInputLayout = KTextInputLayout { withId(R.id.countryInputLayout) }
    val countryInput = KEditText { withId(R.id.countryInput) }

    val stateInputLayout = KTextInputLayout { withId(R.id.stateInputLayout) }
    val stateInput = KEditText { withId(R.id.stateInput) }

    val postalCodeInputLayout = KTextInputLayout { withId(R.id.postalCodeInputLayout) }
    val postalCodeInput = KEditText { withId(R.id.postalCodeInput) }

    val addressLine1InputLayout = KTextInputLayout { withId(R.id.addressLine1InputLayout) }
    val addressLine1Input = KEditText { withId(R.id.addressLine1Input) }

    val addressLine2InputLayout = KTextInputLayout { withId(R.id.addressLine2InputLayout) }
    val addressLine2Input = KEditText { withId(R.id.addressLine2Input) }

    val addressLine3InputLayout = KTextInputLayout { withId(R.id.addressLine3InputLayout) }
    val addressLine3Input = KEditText { withId(R.id.addressLine3Input) }
}
