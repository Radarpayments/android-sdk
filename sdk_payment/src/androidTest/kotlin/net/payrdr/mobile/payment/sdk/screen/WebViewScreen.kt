package net.payrdr.mobile.payment.sdk.screen

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.web.KWebView
import net.payrdr.mobile.payment.sdk.R
import net.payrdr.mobile.payment.sdk.payment.ActivityWebChallenge

object WebViewScreen : KScreen<WebViewScreen>() {
    override val layoutId: Int
        get() = R.layout.activity_web_challenge
    override val viewClass: Class<*>
        get() = ActivityWebChallenge::class.java

    val webView = KWebView { withId(R.id.web_view) }
}
