package net.payrdr.mobile.payment.sdk.screen

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.web.KWebView
import net.payrdr.mobile.payment.sdk.R
import net.payrdr.mobile.payment.sdk.payment.Activity3DS1Challenge

object ThreeDS1Screen : KScreen<ThreeDS1Screen>() {
    override val layoutId: Int
        get() = R.layout.activity_web_challenge
    override val viewClass: Class<*>
        get() = Activity3DS1Challenge::class.java

    val webView = KWebView { withId(R.id.web_view) }

}
