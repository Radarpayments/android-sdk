# PAYRDR Android SDK

Payment SDK for Android: a ready-made payment form (card, saved cards, Google Pay, card scanning) and card data encryption for the payment gateway.

- **sdk-payment** — payment flow with the prebuilt UI. Recommended for most integrations.
- **sdk-core** — card validation and cryptogram (seToken) generation, for your own payment UI.

## Requirements

- Android 5.0 (API 21)+

## Installation

The SDK is published on Maven Central:

```kotlin
dependencies {
    implementation("io.github.radarpayments:sdk-payment:3.1.0")
}
```

All other dependencies come transitively. For your own UI, use `io.github.radarpayments:sdk-core:3.1.0` only.

If another library in your app also ships `libc++_shared.so` (React Native, Flutter, game engines), add:

```kotlin
android {
    packagingOptions {
        pickFirst("**/libc++_shared.so")
    }
}
```

The release AARs are also attached to each [GitHub release](https://github.com/Radarpayments/android-sdk/releases); with them the transitive dependencies have to be added by hand.

## Quick start

Initialize the SDK once in `Application.onCreate`: after process death Android recreates the SDK screens before any activity of the app.

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SDKPayment.init(SDKPaymentConfig(baseURL = "https://<gateway-host>/payment"))
    }
}
```

Start the payment from an activity or a fragment and handle the result in `onActivityResult`:

```kotlin
SDKPayment.checkout(this, CheckoutConfig.MdOrder(mdOrder))

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    SDKPayment.handleCheckoutResult(requestCode, data, object : ResultPaymentCallback<PaymentResult> {
        override fun onResult(result: PaymentResult) {
            // result.isSuccess, result.exception:
            // SDKCanceledByUserException when the user cancels,
            // SDKDeclinedException (message, actionCode, errorType) when the payment is declined
        }
    })
}
```

The order (`mdOrder`) or session (`CheckoutConfig.SessionId`) is registered by your backend; card data never reaches it.

## Migration from 3.0.x

3.1.0 merges the former `sdk_forms` module into `sdk-payment` and moves distribution to Maven Central.

- Replace the AARs in `libs` with the Maven Central dependency above. Remove `sdk_forms-release.aar` and
  `sdk_threeds-release.aar`, and the dependencies you added by hand for the SDK: they now come transitively.
- Update the imports:

  | 3.0.x | 3.1.0 |
  |---|---|
  | `net.payrdr.mobile.payment.sdk.form.SDKException` | `net.payrdr.mobile.payment.sdk.SDKException` |
  | `net.payrdr.mobile.payment.sdk.form.component.impl.SSLContextConfig` | `net.payrdr.mobile.payment.sdk.component.impl.SSLContextConfig` |

- `SDKForms` and its builders and callbacks are no longer public. Pay with `SDKPayment.checkout`; to build your own
  payment UI, generate the cryptogram with `sdk-core`.
- A failed payment now comes with `result.exception`. A declined payment returns `SDKDeclinedException` with the
  gateway message (in the device language), `actionCode` and `errorType`. If your code treated
  `exception == null` as "not failed", check `result.isSuccess` instead.

## Documentation

Full integration guide: https://github.com/Radarpayments/mobile-sdk-doc/wiki

## License

[MIT](LICENSE.md) © BPC
