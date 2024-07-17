package net.payrdr.mobile.payment.sdk.screen

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.web.KWebView
import net.payrdr.mobile.payment.sdk.R
import net.payrdr.mobile.payment.sdk.threeds.impl.activity.WebChallengeFragment

object ThreeDSSDKWebViewScreen: KScreen<ThreeDSSDKWebViewScreen>() {
    override val layoutId: Int
        get() = R.layout.fragment_web_challenge
    override val viewClass: Class<*>
        get() = WebChallengeFragment::class.java

    val webView = KWebView {
        withId(R.id.activity_web_challenge_browser)
    }
}
