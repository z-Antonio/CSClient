package com.csclient.android.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.csclient.android.utils.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

abstract class AbstractClient<I : IInterface>(val context: Context) : IClient<I> {

    companion object {
        const val STATUS_PENDING = 0
        const val STATUS_BINDING = 1
        const val STATUS_CONNECTED = 2

        private const val MSG_ACTION = 1
    }

    private val status = AtomicInteger(STATUS_PENDING)
    private var client: I? = null
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
                    client?.let(it)
                }
            } catch (e: Exception) {
                unbindService()
                e.message?.let { log(it) }
                e.stackTrace.forEach { log(it.toString()) }
            }
        }
    }

    private var observer: LifecycleObserver? = null

    init {
        context.isTypeOf<LifecycleOwner> { owner ->
            context.launchMain {
                observer = LifecycleObserver(owner)
            }
        }
        bindService()
    }

    override fun emit(action: (I) -> Unit) {
        handler.sendMessage(handler.obtainMessage(MSG_ACTION, action))
    }

    private fun bindService() {
        if (status.compareAndSet(STATUS_PENDING, STATUS_BINDING)) {
            val intent = Intent().apply {
                component = serviceComponent()
            }
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun unbindService() {
        if (status.getAndSet(STATUS_PENDING) != STATUS_PENDING) {
            context.unbindService(serviceConnection)
            client = null
            lock = CountDownLatch(1)
        }
    }

    fun log(msg: String) {
        logW(msg)
    }

    inner class LifecycleObserver(private val owner: LifecycleOwner) : LifecycleEventObserver {

        init {
            owner.lifecycle.addObserver(this)
        }

        private fun detachObserver() {
            owner.lifecycle.removeObserver(this)
            unbindService()
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            val currentState: Lifecycle.State = owner.lifecycle.currentState
            if (currentState == Lifecycle.State.DESTROYED) {
                detachObserver()
                return
            }
            if (currentState == Lifecycle.State.CREATED) {
                bindService()
                return
            }
        }

    }
}