package com.csclient.android.app.service

import android.util.Log
import com.csclient.android.app.ITest
import com.csclient.android.service.AbstractService

class MainService: AbstractService<ITest.Stub>() {

    override val stub: ITest.Stub = object : ITest.Stub() {
        override fun test(type: Int, args: String?) {
            Log.w("aaaa", "#test >>>> $type, $args")
        }
    }

}