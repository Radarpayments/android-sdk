package net.payrdr.mobile.payment.sdk.screen

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.text.KButton
import net.payrdr.mobile.payment.sdk.R
import net.payrdr.mobile.payment.sdk.threeds.impl.activity.TextChallengeFragment
import net.payrdr.mobile.payment.sdk.threeds.R as threeDSR

object ThreeDSScreen : KScreen<ThreeDSScreen>() {
    override val layoutId: Int?
        get() = R.layout.fragment_text_challenge
    override val viewClass: Class<*>?
        get() = TextChallengeFragment::class.java

    val dataEntry = KEditText { withId(threeDSR.id.activity_text_challenge_dataEntry) }

    val submit = KButton { withId(threeDSR.id.activity_text_challenge_submit) }

    val cancel = KButton { withId(threeDSR.id.otp_page_toolbar_cancel) }
}
