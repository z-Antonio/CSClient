package com.csclient.android

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.IInterface
import com.csclient.android.client.AbstractClient
import com.csclient.android.client.IClient
import com.csclient.android.utils.parse

object Client {
    private val cache = HashMap<Class<out IInterface>, AbstractClient<out IInterface>>()

    private val serviceInfo: MutableMap<Class<*>, ComponentName> by lazy { mutableMapOf() }

    private fun parseServiceInfo(context: Context) {
        context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_SERVICES
        ).services?.forEach { it.parse(serviceInfo) }
    }

    fun register(clazz: Class<*>, component: ComponentName) {
        serviceInfo[clazz] = component
    }

    fun <I : IInterface> fetch(context: Context, clazz: Class<I>): IClient<I> {
        return synchronized(this@Client) {
            parseServiceInfo(context)
            cache[clazz] ?: object : AbstractClient<I>(context) {
                override fun asInterface(service: IBinder?): I? =
                    clazz.declaredClasses.find { it.name.endsWith("Stub") }?.methods?.find {
                        it.name.contains("asInterface")
                    }?.invoke("invoke", service) as? I?

                override fun serviceComponent(): ComponentName? = serviceInfo?.get(clazz)

            }.apply { cache[clazz] = this }
        } as IClient<I>
    }

    inline fun <reified I : IInterface> getClient(context: Context): IClient<I> {
        return fetch(context, I::class.java)
    }

    /**
     * 注意：action()动作是在非主线程中执行
     */
    inline fun <reified I : IInterface> get(context: Context, noinline action: (I) -> Unit) {
        getClient<I>(context).emit(action as (IInterface) -> Unit)
    }

}