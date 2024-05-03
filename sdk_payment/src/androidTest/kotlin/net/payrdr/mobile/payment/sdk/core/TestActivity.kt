package net.payrdr.mobile.payment.sdk.core

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.payrdr.mobile.payment.sdk.test.R

class TestActivity : AppCompatActivity() {

    val resultHandlerHelper =  ResultHandlerHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        resultHandlerHelper.onActivityResult(requestCode, resultCode, data)
    }

}
