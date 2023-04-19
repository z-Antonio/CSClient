package com.csclient.android.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.csclient.android.utils.logW

abstract class AbstractService<Stub : Binder> : LifecycleService() {

    abstract val stub: Stub

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        log("service onBind")
        return stub
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent).apply {
            log("service onUnbind")
        }
    }

    override fun onCreate() {
        super.onCreate()
        log("service is onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        log("service is onDestroy")
    }

    fun log(msg: String) {
        logW(msg)
    }

}