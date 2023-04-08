package com.csclient.android.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Binder
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import java.lang.reflect.ParameterizedType

var TAG: String = "CSClient"

fun ServiceInfo.parse(map: MutableMap<Class<*>, ComponentName>) {
    val component = ComponentName(this.packageName, this.name)
    Class.forName(component.className).genericSuperclass.isTypeOf<ParameterizedType> { type ->
        type.actualTypeArguments.firstOrNull().isTypeOf<Class<Binder>> {
            it.interfaces.firstOrNull()?.let { clz ->
                map[clz] = component
            }
        }
    }
}

inline fun <reified T> Any?.isTypeOf(action: (T) -> Unit) {
    if (this is T) {
        action(this)
    }
}

fun Any.logW(msg: String) {
    val clz = this::class.java.simpleName
    Log.w(TAG, "$clz >>> $msg")
}

fun Context.launchMain(block: suspend () -> Unit): Job {
    return (if (this is LifecycleOwner) lifecycleScope else GlobalScope).launch {
        withContext(Dispatchers.Main) {
            kotlin.runCatching {
                block()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
}