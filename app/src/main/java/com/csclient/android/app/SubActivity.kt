package com.csclient.android.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.csclient.android.Client

class SubActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        Client.get<ITest>(this) {
            it.test(MainActivity.ID, "hello Sub ${MainActivity.ID}!")
        }
    }
}