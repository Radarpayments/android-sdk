package net.payrdr.mobile.payment.sdk.form.ui.adapter

import android.text.Editable
import android.text.TextWatcher

internal abstract class TextWatcherAdapter : TextWatcher {

    override fun afterTextChanged(s: Editable?) {
        // override if necessary
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // override if necessary
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // override if necessary
    }
}
