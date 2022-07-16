package net.payrdr.mobile.payment.sdk.form.ui.widget

import android.content.Context
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.layout_bank_card.view.cardBankLogo
import net.payrdr.mobile.payment.sdk.form.R
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

    init {
        inflate(context, R.layout.layout_bank_card, this)
        initBackground()
    }

    private fun initBackground() {
        val cornerRadius = resources.getDimension(R.dimen.payrdr_card_corner_radius)
        val outerRadius = floatArrayOf(
            cornerRadius, cornerRadius, cornerRadius, cornerRadius,
            cornerRadius, cornerRadius, cornerRadius, cornerRadius
        )

        val shapeDrawable = ShapeDrawable().apply {
            shape = RoundRectShape(outerRadius, null, null)
        }
        shapeDrawable.paint.color =
            ContextCompat.getColor(context, R.color.background_logo_color)
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        val drawable = LayerDrawable(arrayOf(shapeDrawable))
        background = drawable
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
     * Set the card style for an unknown bank.
     */
    fun setupUnknownBrand() {
        setBankLogoUrl(null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val params = layoutParams
        val formMargin = if (params is MarginLayoutParams) {
            params.leftMargin + params.rightMargin
        } else {
            0
        }
        val size = min(
            resources.getDimensionPixelSize(R.dimen.payrdr_card_width) + formMargin,
            width + formMargin
        )
        val widthSpec = MeasureSpec.makeMeasureSpec(size, EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(size, EXACTLY)
        super.onMeasure(widthSpec, heightSpec)
    }
}
