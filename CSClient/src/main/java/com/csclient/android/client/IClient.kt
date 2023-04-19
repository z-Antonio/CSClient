package com.csclient.android.client

import android.content.ComponentName
import android.os.IBinder
import android.os.IInterface

interface IClient<I : IInterface> {

    fun emit(action: (I) -> Unit)

    fun asInterface(service: IBinder?): I?

    fun serviceComponent(): ComponentName?

}