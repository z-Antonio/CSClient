package com.csclient.android.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import com.csclient.android.utils.isTypeOf
import com.csclient.android.utils.logW
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

abstract class AbstractClient<I : IInterface>(ctx: Context) : IClient<I> {

    companion object {
        const val STATUS_PENDING = 0
        const val STATUS_BINDING = 1
        const val STATUS_CONNECTED = 2

        private const val MSG_ACTION = 1
    }

    private val context: Context = ctx.applicationContext
    private var client: I? = null

    private val status = AtomicInteger(STATUS_PENDING)
    private var lock = CountDownLatch(1)

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            status.set(STATUS_CONNECTED)
            client = asInterface(service)
            lock.countDown()
            log("service is connected. lock is ${lock.count}")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            unbindService()
            log("service is disconnected")
        }
    }

    private val handler = object : Handler(HandlerThread("Client-Thread").apply {
        start()
    }.looper) {
        override fun handleMessage(msg: Message) {
            bindService()
            lock.await()
            try {
                msg.obj.isTypeOf<(I) -> Unit> {
                    log("execute  ")
                    client?.let(it)
                }
            } catch (e: Exception) {
                unbindService()
                e.message?.let { log(it) }
                e.stackTrace.forEach { log(it.toString()) }
            }
        }
    }

    override fun emit(action: (I) -> Unit) {
        handler.sendMessage(handler.obtainMessage(MSG_ACTION, action))
    }

    private fun bindService() {
        if (status.compareAndSet(STATUS_PENDING, STATUS_BINDING)) {
            log("#bindService >>>")
            val intent = Intent().apply {
                component = serviceComponent()
            }
            if (!context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)) {
                status.set(STATUS_PENDING)
            }
        }
    }

    private fun unbindService() {
        if (status.getAndSet(STATUS_PENDING) != STATUS_PENDING) {
            log("#unbindService >>>")
            context.unbindService(serviceConnection)
            client = null
            lock = CountDownLatch(1)
        }
    }

    fun log(msg: String) {
        logW(msg)
    }
}