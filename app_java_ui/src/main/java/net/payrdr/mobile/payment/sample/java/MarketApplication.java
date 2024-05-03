package net.payrdr.mobile.payment.sample.java;

import android.app.Application;

import net.payrdr.mobile.payment.sdk.form.SDKFormsConfigBuilder;
import net.payrdr.mobile.payment.sdk.form.SDKForms;

public class MarketApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SDKForms.INSTANCE.init(new SDKFormsConfigBuilder()
                .keyProviderUrl("https://dev.bpcbt.com/payment/se/keys.do", null)
                .build()
        );
    }
}
