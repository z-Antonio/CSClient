package com.csclient.android.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.csclient.android.Client
import com.csclient.android.app.service.MainService

class MainActivity : AppCompatActivity() {
    companion object {
        var ID = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startService(Intent(this, MainService::class.java))
        findViewById<View>(R.id.id_tv).setOnClickListener {
            startActivity(Intent(this, SubActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        Client.get<ITest>(this) {
            it.test(ID, "hello ${ID++}!")
        }
    }
}