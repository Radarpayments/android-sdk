package net.payrdr.mobile.payment.sample.java;

import android.app.Application;

import net.payrdr.mobile.payment.sdk.form.SDKConfigBuilder;
import net.payrdr.mobile.payment.sdk.form.SDKForms;

public class MarketApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SDKForms.INSTANCE.init(new SDKConfigBuilder()
                .keyProviderUrl("https://ecommerce.radarpayments.com/payment/se/keys.do")
                .build()
        );
    }
}
