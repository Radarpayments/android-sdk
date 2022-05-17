package net.payrdr.mobile.payment.sdk.form.ui.widget

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.widget.FrameLayout
import androidx.core.content.ContextCompat.getColor
import kotlinx.android.synthetic.main.layout_bank_card.view.cardBankLogo
import kotlinx.android.synthetic.main.layout_bank_card.view.cardExpiry
import kotlinx.android.synthetic.main.layout_bank_card.view.cardHolder
import kotlinx.android.synthetic.main.layout_bank_card.view.cardNumber
import kotlinx.android.synthetic.main.layout_bank_card.view.cardSystem
import net.payrdr.mobile.payment.sdk.core.model.ExpiryDate
import net.payrdr.mobile.payment.sdk.core.utils.noSpaces
import net.payrdr.mobile.payment.sdk.core.utils.parseColor
import net.payrdr.mobile.payment.sdk.form.R
import net.payrdr.mobile.payment.sdk.form.ui.helper.CardLogoAssetsResolver
import kotlin.math.min

/**
 * UI component for displaying bank card data.
 */
@Suppress("TooManyFunctions")
class BankCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var gradientDrawable: GradientDrawable

    init {
        inflate(context, R.layout.layout_bank_card, this)
        initBackground()
        setupEmptyStyle()
        setNumber("")
        setExpiry("")
    }

    /**
     * Set the expiration date of the card.
     *
     * @param expiry card expiration date string in "MM/YY" format.
     */
    fun setExpiry(expiry: String) {
        cardExpiry.setExpiry(expiry)
    }

    /**
     * Set the expiration date of the card.
     *
     * @param expiry card expiry date.
     */
    fun setExpiry(expiry: ExpiryDate) {
        cardExpiry.setExpiry(expiry)
    }

    private fun initBackground() {
        val cornerRadius = resources.getDimension(R.dimen.payrdr_card_corner_radius)
        val elevation = resources.getDimension(R.dimen.payrdr_card_elevation).toInt()
        val shadowColor = getColor(context, R.color.payrdr_color_shadow)
        val outerRadius = floatArrayOf(
            cornerRadius, cornerRadius, cornerRadius, cornerRadius,
            cornerRadius, cornerRadius, cornerRadius, cornerRadius
        )

        val shapeDrawablePadding = Rect().apply {
            left = elevation
            right = elevation
            top = elevation * 2
            bottom = elevation * 2
        }

        val dy = elevation / SHADOW_DIVIDER

        gradientDrawable = GradientDrawable().also {
            it.orientation = GradientDrawable.Orientation.TL_BR
            it.cornerRadius = cornerRadius
        }

        val shapeDrawable = ShapeDrawable().apply {
            setPadding(shapeDrawablePadding)
            paint.color = shadowColor
            paint.setShadowLayer(
                cornerRadius / 2,
                0f,
                dy.toFloat(),
                shadowColor
            )
            shape = RoundRectShape(outerRadius, null, null)
        }
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        val drawable = LayerDrawable(arrayOf(shapeDrawable, gradientDrawable))
        drawable.setLayerInset(0, elevation, elevation * 2, elevation, elevation * 2)
        background = drawable
    }

    /**
     * Setting the card number.
     *
     * @param number Card number.
     */
    fun setNumber(number: String) {
        val clearNumber = number.noSpaces(NUMBER_MAX_LENGTH)
            .replace("[^\\d.]".toRegex(), "•")
        val formatted = StringBuilder(NUMBER_MASK)
        formatted.replace(0, clearNumber.length, clearNumber)
        for (position in SPACE_POSITIONS) {
            if (formatted.length > position) {
                formatted.insert(position, SPACE).toString()
            }
        }
        cardNumber.text = formatted
    }

    /**
     * Setting the display of the card expiration date field.
     *
     * @param enabled if true then the field is displayed, otherwise not.
     */
    fun enableExpiry(enabled: Boolean) {
        cardExpiry.visibility = if (enabled) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Setting the display of the cardholder field.
     *
     * @param enabled if true then the field is displayed, otherwise not.
     */
    fun enableHolderName(enabled: Boolean) {
        cardHolder.visibility = if (enabled) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Set the name of the cardholder.
     *
     * @param name Cardholder Name.
     */
    fun setHolderName(name: String) {
        cardHolder.text = name
    }

    /**
     * Setting a link to the bank logo.
     *
     * @param url logo link.
     */
    fun setBankLogoUrl(url: String?) {
        SetupTitleTask(cardBankLogo).execute(url)
    }

    /**
     * Setting the type of payment system.
     *
     * When [preferLight] is set to true, a suitable light payment system logo
     * will be searched first.
     *
     * @param system the name of the payment system.
     * @param preferLight preference for using the illuminated logo of the payment system.
     */
    fun setPaymentSystem(system: String, preferLight: Boolean = false) {
        val logoResource = CardLogoAssetsResolver.resolveByName(context, system, preferLight)
        if (logoResource != null) {
            cardSystem.setImageAsset(logoResource)
        } else {
            cardSystem.setImageDrawable(null)
        }
    }

    /**
     * Sets the text color of the fields on the map.
     *
     * @param color color in the format "#ffffff" or in the abbreviated form "#fff".
     */
    fun setTextColor(color: String) = setTextColor(color.parseColor())

    /**
     * Sets the text color of the fields on the map.
     *
     * @param color color.
     */
    fun setTextColor(color: Int) {
        cardHolder.setTextColor(color)
        cardHolder.setHintTextColor(color)
        cardNumber.setTextColor(color)
        cardNumber.setHintTextColor(color)
        cardExpiry.setTextColor(color)
        cardExpiry.setHintTextColor(color)
    }

    /**
     * Setting the background gradient of the map.
     *
     * @param startColor the starting color of the gradient, in the format "#ffffff" or shorthand
     * "#fff".
     * @param endColor the end color of the gradient, in the format "#ffffff" or shorthand
     * "#fff".
     */
    fun setBackground(startColor: String, endColor: String) =
        setBackground(
            startColor.parseColor(),
            endColor.parseColor()
        )

    /**
     * Setting the background gradient of the map.
     *
     * @param startColor the start color of the gradient.
     * @param endColor the end color of the gradient.
     */
    fun setBackground(startColor: Int, endColor: Int) {
        gradientDrawable.colors = intArrayOf(startColor, endColor)
        drawableStateChanged()
    }

    /**
     * Set the card style for an unknown bank.
     */
    fun setupUnknownBrand() {
        val color = getColor(context, R.color.payrdr_color_card_background)
        setBackground(color, color)
        setTextColor(getColor(context, R.color.payrdr_color_text))
        setPaymentSystem("")
        setBankLogoUrl(null)
    }

    private fun setupEmptyStyle() {
        val color = getColor(context, R.color.payrdr_color_card_background)
        setBackground(color, color)
        setTextColor(getColor(context, R.color.payrdr_color_text))
        setPaymentSystem("")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val params = layoutParams
        val formMargin = if (params is MarginLayoutParams) {
            params.leftMargin + params.rightMargin
        } else {
            0
        }
        val size = min(
            MeasureSpec.getSize(widthMeasureSpec) - formMargin,
            resources.getDimensionPixelSize(R.dimen.payrdr_card_max_width) - formMargin
        )
        val widthSpec = MeasureSpec.makeMeasureSpec(size, EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec((size / CARD_RATIO).toInt(), EXACTLY)
        super.onMeasure(widthSpec, heightSpec)
    }

    companion object {
        private const val SHADOW_DIVIDER = 3
        private const val CARD_RATIO = 1.50f
        private const val SPACE = " "
        private const val NUMBER_MASK = "••••••••••••••••  "
        private const val NUMBER_MAX_LENGTH = 19
        private val SPACE_POSITIONS = intArrayOf(4, 9, 14)
    }
}
