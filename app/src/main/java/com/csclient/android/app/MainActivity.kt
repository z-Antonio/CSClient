package com.csclient.android.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.csclient.android.Client

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Client.get<ITest>(this) {
            it.test(1, "hello!")
        }
    }
}