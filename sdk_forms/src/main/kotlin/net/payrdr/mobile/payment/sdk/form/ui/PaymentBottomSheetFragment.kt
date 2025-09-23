package net.payrdr.mobile.payment.sdk.form.ui

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.wallet.button.ButtonConstants
import com.google.android.gms.wallet.button.ButtonOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_bottom_sheet_payment.allPaymentMethodLayout
import kotlinx.android.synthetic.main.fragment_bottom_sheet_payment.cardList
import kotlinx.android.synthetic.main.fragment_bottom_sheet_payment.dismissButton
import kotlinx.android.synthetic.main.fragment_bottom_sheet_payment.googlePayButton
import kotlinx.android.synthetic.main.fragment_bottom_sheet_payment.orPayByCardLayout
import net.payrdr.mobile.payment.sdk.form.Constants
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayUtils
import net.payrdr.mobile.payment.sdk.form.gpay.GoogleTokenizationSpecificationType.PAYMENT_GATEWAY
import net.payrdr.mobile.payment.sdk.form.gpay.TokenizationSpecification.Companion.tokenizationSpecificationCreate
import net.payrdr.mobile.payment.sdk.form.gpay.TokenizationSpecificationParameters.Companion.tokenizationSpecificationParametersCreate
import net.payrdr.mobile.payment.sdk.form.model.Card
import net.payrdr.mobile.payment.sdk.form.model.GooglePayPaymentConfig
import net.payrdr.mobile.payment.sdk.form.model.PaymentConfig
import net.payrdr.mobile.payment.sdk.form.model.Theme
import net.payrdr.mobile.payment.sdk.form.ui.adapter.CardListAdapter
import net.payrdr.mobile.payment.sdk.form.ui.helper.UIDelegate
import net.payrdr.mobile.payment.sdk.form.utils.finishWithUserCancellation

/**
 * Bottom Sheet to start the payment process via all payment method.
 */
class PaymentBottomSheetFragment : BottomSheetDialogFragment() {

    private val cardsAdapter = CardListAdapter()
    private var googlePayPaymentConfig: GooglePayPaymentConfig? = null
    private lateinit var config: PaymentConfig

    companion object {
        /**
         * Key for config saving
         */
        const val CONFIG_KEY = "CONFIG_KEY"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_bottom_sheet_payment, container, false)

    internal fun show(manager: FragmentManager, tag: String?, paymentConfig: PaymentConfig) {
        config = paymentConfig
        super.show(manager, tag)
    }

    internal fun setGooglePayPaymentConfig(googlePayPaymentConfig: GooglePayPaymentConfig?) {
        googlePayPaymentConfig?.let {
            this.googlePayPaymentConfig = googlePayPaymentConfig
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(CONFIG_KEY, config)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.googlePayPaymentConfig?.let { config ->
            checkAvailableGPayPayment(config = config)
        } ?: run {
            Log.d("PAYRDRSDK", "GPay not supported by server")
            disableGoogleButton()
        }

        savedInstanceState?.getParcelable<PaymentConfig>(CONFIG_KEY)?.also {
            config = it
        }

        allPaymentMethodLayout.setOnClickListener {
            SDKForms.cryptogram(requireActivity(), config)
            dismiss()
        }

        dismissButton.setOnClickListener {
            dismiss()
            requireActivity().finishWithUserCancellation()
        }
        cardsAdapter.cardSelectListener = object : CardListAdapter.CardSelectListener {
            override fun onCardSelected(card: Card) {
                openSavedCard(card)
            }

            override fun onCardDeleted(card: Card) {
                Log.i("PaymentBottomSheet", "onCardDeleted")
            }
        }
        cardsAdapter.newCardSelectListener = object : CardListAdapter.NewCardSelectListener {
            override fun onCardSelected() {
                openNewCard()
            }
        }
        cardsAdapter.cards = config.cards.toList()
        cardList.apply {
            adapter = cardsAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        requireActivity().finishWithUserCancellation()
    }

    private fun openNewCard() {
        requireActivity().startActivityForResult(
            CardNewActivity.prepareIntent(requireActivity(), config),
            Constants.REQUEST_CODE_CRYPTOGRAM
        )
        dismiss()
    }

    private fun openSavedCard(card: Card) {
        requireActivity().startActivityForResult(
            CardSelectedActivity.prepareIntent(requireActivity(), config, card),
            Constants.REQUEST_CODE_CRYPTOGRAM
        )
        dismiss()
    }

    private fun checkAvailableGPayPayment(config: GooglePayPaymentConfig) {
        GooglePayUtils.possiblyShowGooglePayButton(
            context = this.requireContext(),
            isReadyToPayJson = GooglePayUtils.getIsReadyToPayJson(),
            paymentsClient = GooglePayUtils.createPaymentsClient(
                this.requireContext(),
                GooglePayUtils.getEnvironment(config.testEnvironment)
            ),
            callback = object : GooglePayUtils.GooglePayCheckCallback {
                override fun onNoGooglePlayServices() {
                    disableGoogleButton()
                    Log.d("PAYRDRSDK", "GPay not supported by device")
                }

                override fun onNotReadyToRequest() {
                    disableGoogleButton()
                    Log.d("PAYRDRSDK", "GPay not supported by device")
                }

                override fun onReadyToRequest() {
                    initializeGooglePayButton(config = config)
                    enableGoogleButton {
                        SDKForms.cryptogram(
                            this@PaymentBottomSheetFragment,
                            config
                        )
                        dismiss()
                    }
                }
            }
        )
    }

    private fun disableGoogleButton() {
        googlePayButton.setOnClickListener(null)
        googlePayButton.visibility = View.GONE
        orPayByCardLayout.visibility = View.GONE
    }

    private fun enableGoogleButton(listener: View.OnClickListener) {
        googlePayButton.setOnClickListener(listener)
        googlePayButton.visibility = View.VISIBLE
        orPayByCardLayout.visibility = View.VISIBLE
    }

    private fun initializeGooglePayButton(config: GooglePayPaymentConfig) {
        googlePayButton.initialize(
            ButtonOptions
                .newBuilder()
                .setButtonTheme(googlePayButtonTheme(config = config))
                .setButtonType(ButtonConstants.ButtonType.PAY)
                .setAllowedPaymentMethods(
                    GooglePayUtils.allowedPaymentMethods(
                        tokenizationSpecification = tokenizationSpecificationCreate {
                            type = PAYMENT_GATEWAY
                            parameters = tokenizationSpecificationParametersCreate {
                                gateway = config.gateway
                                gatewayMerchantId = config.gatewayMerchantId
                            }
                        }
                    ).toString()
                )
                .build()
        )
    }

    private fun googlePayButtonTheme(config: GooglePayPaymentConfig): Int =
        when (config.theme) {
            Theme.DEFAULT, Theme.SYSTEM -> {
                if (UIDelegate.isDarkTheme(resources)) ButtonConstants.ButtonTheme.LIGHT
                else ButtonConstants.ButtonTheme.DARK
            }

            Theme.LIGHT -> {
                ButtonConstants.ButtonTheme.LIGHT
            }

            Theme.DARK -> {
                ButtonConstants.ButtonTheme.DARK
            }
        }
}
