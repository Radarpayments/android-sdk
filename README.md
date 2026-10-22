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
    implementation("io.github.radarpayments:sdk-payment:3.0.14")
}
```

All other dependencies come transitively. For your own UI, use `io.github.radarpayments:sdk-core:3.0.14` only.

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
            // result.isSuccess, result.exception (SDKCanceledByUserException when the user cancels)
        }
    })
}
```

The order (`mdOrder`) or session (`CheckoutConfig.SessionId`) is registered by your backend; card data never reaches it.

## Documentation

Full integration guide: https://github.com/Radarpayments/mobile-sdk-doc/wiki

## License

[MIT](LICENSE.md) © BPC
