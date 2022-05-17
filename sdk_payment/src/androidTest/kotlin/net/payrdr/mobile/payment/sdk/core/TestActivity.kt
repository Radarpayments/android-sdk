package net.payrdr.mobile.payment.sdk.core

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.payrdr.mobile.payment.sdk.test.R

class TestActivity : AppCompatActivity() {

    var onActivityResultListener: ((requestCode: Int, resultCode: Int, data: Intent?) -> Unit)? =
        null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResultListener?.invoke(requestCode, resultCode, data)
    }
}
