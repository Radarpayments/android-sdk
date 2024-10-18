package net.payrdr.mobile.payment.sample.kotlin.core

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.bindingIdInput
import kotlinx.android.synthetic.main.activity_main.cardCodeInput
import kotlinx.android.synthetic.main.activity_main.cardExpiryInput
import kotlinx.android.synthetic.main.activity_main.cardHolderInput
import kotlinx.android.synthetic.main.activity_main.cardNumberInput
import kotlinx.android.synthetic.main.activity_main.generateWithBinding
import kotlinx.android.synthetic.main.activity_main.generateWithCard
import kotlinx.android.synthetic.main.activity_main.mdOrderInput
import kotlinx.android.synthetic.main.activity_main.pubKeyInput
import net.payrdr.mobile.payment.sdk.core.SDKCore
import net.payrdr.mobile.payment.sdk.core.TokenResult
import net.payrdr.mobile.payment.sdk.core.model.BindingParams
import net.payrdr.mobile.payment.sdk.core.model.CardParams
import net.payrdr.mobile.payment.sdk.core.model.SDKCoreConfig
import net.payrdr.mobile.payment.sdk.core.validation.BaseValidator
import net.payrdr.mobile.payment.sdk.core.validation.CardCodeValidator
import net.payrdr.mobile.payment.sdk.core.validation.CardExpiryValidator
import net.payrdr.mobile.payment.sdk.core.validation.CardHolderValidator
import net.payrdr.mobile.payment.sdk.core.validation.CardNumberValidator
import net.payrdr.mobile.payment.sdk.core.validation.OrderNumberValidator

class MainActivity : AppCompatActivity() {
    // Initialization of validators for card information input fields.
    private val cardNumberValidator by lazy { CardNumberValidator(this) }
    private val cardExpiryValidator by lazy { CardExpiryValidator(this) }
    private val cardCodeValidator by lazy { CardCodeValidator(this) }
    private val cardHolderValidator by lazy { CardHolderValidator(this) }
    private val orderNumberValidator by lazy { OrderNumberValidator(this) }
    private val sdkCore by lazy { SDKCore(context = this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Setting validators on the fields for entering information about the card.
        cardNumberInput.setupValidator(cardNumberValidator)
        cardExpiryInput.setupValidator(cardExpiryValidator)
        cardCodeInput.setupValidator(cardCodeValidator)
        cardHolderInput.setupValidator(cardHolderValidator)
        mdOrderInput.setupValidator(orderNumberValidator)

        generateWithCard.setOnClickListener {
            // Creating an object and initializing fields for a new card.
            val params = CardParams(
                mdOrder = mdOrderInput.text.toString(),
                pan = cardNumberInput.text.toString(),
                cvc = cardCodeInput.text.toString(),
                expiryMMYY = cardExpiryInput.text.toString(),
                cardHolder = cardHolderInput.text.toString(),
                pubKey = pubKeyInput.text.toString()
            )
            // Method call to get cryptogram for new card.
            proceedResult(sdkCore.generateWithConfig(SDKCoreConfig(params)))
        }

        generateWithBinding.setOnClickListener {
            // Creating an object and initializing fields for the binding card.
            val params = BindingParams(
                mdOrder = mdOrderInput.text.toString(),
                bindingID = bindingIdInput.text.toString(),
                cvc = "123",
                pubKey = pubKeyInput.text.toString()
            )
            // Method call to get the cryptogram for the binding card.
            proceedResult(sdkCore.generateWithConfig(SDKCoreConfig(params)))
        }
    }

    private fun proceedResult(tokenResult: TokenResult) {
        val message = with(tokenResult) {
            if (errors.isEmpty()) token else errors.map { "${it.key} ${it.value}" }
                .joinToString()
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun EditText.setupValidator(validator: BaseValidator<String>) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                validator.validate(s.toString()).let { result ->
                    if (result.isValid) {
                        this@setupValidator.setError(null, null)
                    } else {
                        this@setupValidator.setError(
                            "[${result.errorCode}] ${result.errorMessage}",
                            null
                        )
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }


}