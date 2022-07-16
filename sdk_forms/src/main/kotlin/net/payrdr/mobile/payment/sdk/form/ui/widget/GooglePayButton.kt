package net.payrdr.mobile.payment.sdk.form.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import net.payrdr.mobile.payment.sdk.form.R

/**
 * UI component for displaying the Google Pay payment button.
 */
class GooglePayButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var imageFormat: ImageFormat = ImageFormat.WITHOUT_TEXT
    private var backgroundFormat: BackgroundFormat = BackgroundFormat.SHADOW

    init {
        isClickable = true
        isFocusable = true
        setBackgroundColor(Color.TRANSPARENT)
        context.theme.obtainStyledAttributes(attrs, R.styleable.GooglePayButton, 0, 0).apply {
            try {
                val imageFormatValue =
                    getInteger(R.styleable.GooglePayButton_payrdr_google_pay_button_image_format, 0)
                val backgroundFormatValue =
                    getInteger(
                        R.styleable.GooglePayButton_payrdr_google_pay_button_background_format,
                        0
                    )
                imageFormat = ImageFormat.fromStyle(imageFormatValue)
                backgroundFormat = BackgroundFormat.fromStyle(backgroundFormatValue)
                updateView()
            } finally {
                recycle()
            }
        }
    }

    private fun updateView() {
        val layoutId = when (backgroundFormat) {
            BackgroundFormat.SHADOW -> when (imageFormat) {
                ImageFormat.WITH_TEXT -> R.layout.buy_with_googlepay_button
                ImageFormat.WITHOUT_TEXT -> R.layout.googlepay_button
            }
            BackgroundFormat.OUTLET -> when (imageFormat) {
                ImageFormat.WITH_TEXT -> R.layout.buy_with_googlepay_button_no_shadow
                ImageFormat.WITHOUT_TEXT -> R.layout.googlepay_button_no_shadow
            }
        }
        removeAllViews()
        LayoutInflater.from(context).inflate(layoutId, this)
    }

    /**
     * Sets the aspect ratio of the button.
     *
     * @param format desired format, one of [ImageFormat].
     */
    fun setImageFormat(format: ImageFormat) {
        this.imageFormat = format
        updateView()
    }

    /**
     * Set button background format.
     *
     * @param format desired format, one of [BackgroundFormat].
     */
    fun setBackgroundFormat(format: BackgroundFormat) {
        this.backgroundFormat = format
        updateView()
    }

    /**
     * Possible options for displaying the image of the Google Pay button.
     *
     * @param value value from the markup xml attribute.
     */
    enum class ImageFormat(val value: Int) {
        WITH_TEXT(0),
        WITHOUT_TEXT(1);

        companion object {

            /**
             * Returns forts by value from the markup xml attribute.
             *
             * @param styleValue the value of the markup xml attribute.
             */
            fun fromStyle(styleValue: Int): ImageFormat =
                values().firstOrNull { it.value == styleValue } ?: WITHOUT_TEXT
        }
    }

    /**
     * Possible options for displaying the background of the Google Pay button.
     *
     * @param value value from the markup xml attribute.
     */
    enum class BackgroundFormat(val value: Int) {
        SHADOW(0),
        OUTLET(1);

        companion object {

            /**
             * Returns forts by value from the markup xml attribute.
             *
             * @param styleValue the value of the markup xml attribute.
             */
            fun fromStyle(styleValue: Int): BackgroundFormat =
                values().firstOrNull { it.value == styleValue } ?: SHADOW
        }
    }
}
