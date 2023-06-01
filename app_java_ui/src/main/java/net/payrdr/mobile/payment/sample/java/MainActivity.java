package net.payrdr.mobile.payment.sample.java;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.payrdr.mobile.payment.sdk.core.model.ExpiryDate;
import net.payrdr.mobile.payment.sdk.form.PaymentConfigBuilder;
import net.payrdr.mobile.payment.sdk.form.ResultCryptogramCallback;
import net.payrdr.mobile.payment.sdk.form.SDKException;
import net.payrdr.mobile.payment.sdk.form.SDKForms;
import net.payrdr.mobile.payment.sdk.form.model.CameraScannerOptions;
import net.payrdr.mobile.payment.sdk.form.model.Card;
import net.payrdr.mobile.payment.sdk.form.model.CardSaveOptions;
import net.payrdr.mobile.payment.sdk.form.model.CryptogramData;
import net.payrdr.mobile.payment.sdk.form.model.HolderInputOptions;
import net.payrdr.mobile.payment.sdk.form.model.PaymentConfig;
import net.payrdr.mobile.payment.sdk.form.model.PaymentInfo;
import net.payrdr.mobile.payment.sdk.form.model.PaymentInfoBindCard;
import net.payrdr.mobile.payment.sdk.form.model.PaymentInfoGooglePay;
import net.payrdr.mobile.payment.sdk.form.model.PaymentInfoNewCard;

public class MainActivity extends AppCompatActivity {

    private Locale launchLocale = Locale.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.executeCheckoutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeCheckout();
            }
        });
    }

    private void executeCheckout() {
        // List of binding cards.
        Set<Card> cards = new HashSet();
        cards.add(new Card("492980xxxxxx7724", "ee199a55-cf16-41b2-ac9e-cc1c731edd19", new ExpiryDate(2025, 12)));
        cards.add(new Card("558620xxxxxx6614", "6617c0b1-9976-45d9-b659-364ecac099e2", new ExpiryDate(2024, 6)));
        cards.add(new Card("415482xxxxxx0000", "3d2d320f-ca9a-4713-977c-c852accf8a7b", new ExpiryDate(2019, 1)));
        cards.add(new Card("411790xxxxxx123456", "ceae68c1-cb02-4804-9526-6d6b2f1f2793", null));

        // Order ID is required.
        String order = "00210bac-0ed1-474b-8ec2-5648cdfc4212";
        PaymentConfig paymentConfig = new PaymentConfigBuilder(order)
                // Optional, by default localized translation "Pay".
                .buttonText("Оплатить 200 Ꝑ")
                // Optional, default HIDE.
                .cardSaveOptions(CardSaveOptions.YES_BY_DEFAULT)
                // Optional, default HIDE.
                .holderInputOptions(HolderInputOptions.VISIBLE)
                // Optional, default true.
                .bindingCVCRequired(false)
                // Optional, default ENABLED.
                .cameraScannerOptions(CameraScannerOptions.ENABLED)
                // Optionally, the locale of the payment form is determined automatically.
                .locale(launchLocale)
                // Optional, the default is an empty list.
                .cards(cards)
                // Optionally, a unique payment identifier is generated automatically.
                .uuid("27fb1ebf-895e-4b15-bfeb-6ecae378fe8e")
                // Optionally, the time for generating the payment is set automatically.
                .timestamp(System.currentTimeMillis())
                .build();

        // Calling up the payment screen.
        SDKForms.INSTANCE.cryptogram(MainActivity.this, paymentConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Processing the result.
        SDKForms.INSTANCE.handleCryptogramResult(requestCode, data, new ResultCryptogramCallback<CryptogramData>() {
            @Override
            public void onSuccess(CryptogramData result) {
                // The result of creating a cryptogram.
                if (result.getStatus().isSucceeded()) {
                    PaymentInfo info = result.getInfo();
                    if (info instanceof PaymentInfoNewCard) {
                        PaymentInfoNewCard newCardInfo = (PaymentInfoNewCard) info;
                        log("New card " + newCardInfo.getHolder() + " " + newCardInfo.getSaveCard());
                    } else if (info instanceof PaymentInfoBindCard) {
                        PaymentInfoBindCard bindCard = (PaymentInfoBindCard) info;
                        log("Saved card " + bindCard);
                    } else if (info instanceof PaymentInfoGooglePay) {
                        PaymentInfoGooglePay googlePay = (PaymentInfoGooglePay) info;
                        log("Google pay " + googlePay);
                    }
                    log(result.toString());
                } else if (result.getStatus().isCanceled()) {
                    log("canceled");
                }
            }

            @Override
            public void onFail(SDKException e) {
                // An error has occurred.
                log(e.getMessage() + " " + e.getCause());
            }
        });
    }

    private void log(String message) {

        Log.d("LOG_TAG", message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
