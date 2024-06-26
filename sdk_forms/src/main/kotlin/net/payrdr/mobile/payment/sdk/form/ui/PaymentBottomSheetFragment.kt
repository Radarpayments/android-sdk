package net.payrdr.mobile.payment.sdk.form.ui

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_bottom_sheet_payment.allPaymentMethodLayout
import kotlinx.android.synthetic.main.fragment_bottom_sheet_payment.cardList
import kotlinx.android.synthetic.main.fragment_bottom_sheet_payment.dismissButton
import kotlinx.android.synthetic.main.fragment_bottom_sheet_payment.googlePayButton
import net.payrdr.mobile.payment.sdk.form.Constants
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.SDKForms
import net.payrdr.mobile.payment.sdk.form.gpay.GooglePayUtils
import net.payrdr.mobile.payment.sdk.form.model.Card
import net.payrdr.mobile.payment.sdk.form.model.GooglePayPaymentConfig
import net.payrdr.mobile.payment.sdk.form.model.PaymentConfig
import net.payrdr.mobile.payment.sdk.form.ui.adapter.CardListAdapter
import net.payrdr.mobile.payment.sdk.form.utils.finishWithUserCancellation

/**
 * Bottom Sheet to start the payment process via all payment method.
 */
class PaymentBottomSheetFragment : BottomSheetDialogFragment() {

    private val cardsAdapter = CardListAdapter()
    private lateinit var googlePayPaymentConfig: GooglePayPaymentConfig
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
        savedInstanceState: Bundle?
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

        checkAvailableGPayPayment()

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

    private fun checkAvailableGPayPayment() {
        if (this::googlePayPaymentConfig.isInitialized) {
            GooglePayUtils.possiblyShowGooglePayButton(
                context = this.requireContext(),
                isReadyToPayJson = GooglePayUtils.getIsReadyToPayJson(),
                paymentsClient = GooglePayUtils.createPaymentsClient(
                    this.requireContext(),
                    GooglePayUtils.getEnvironment(googlePayPaymentConfig.testEnvironment)
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
                        enableGoogleButton {
                            SDKForms.cryptogram(
                                this@PaymentBottomSheetFragment,
                                googlePayPaymentConfig
                            )
                            dismiss()
                        }
                    }
                }
            )
        } else {
            Log.d("PAYRDRSDK", "GPay not supported by server")
            disableGoogleButton()
        }
    }

    private fun disableGoogleButton() {
        googlePayButton.setOnClickListener(null)
        googlePayButton.visibility = View.GONE
    }

    private fun enableGoogleButton(listener: View.OnClickListener) {
        googlePayButton.setOnClickListener(listener)
        googlePayButton.visibility = View.VISIBLE
    }
}
