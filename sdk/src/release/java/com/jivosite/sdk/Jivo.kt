package com.jivosite.sdk

import androidx.core.os.bundleOf
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ProcessLifecycleOwner
import com.jivosite.sdk.di.DaggerJivoSdkComponent
import com.jivosite.sdk.di.JivoSdkComponent
import com.jivosite.sdk.di.modules.SdkModule
import com.jivosite.sdk.di.service.WebSocketServiceComponent
import com.jivosite.sdk.di.service.modules.SocketMessageHandlerModule
import com.jivosite.sdk.di.service.modules.StateModule
import com.jivosite.sdk.di.service.modules.WebSocketServiceModule
import com.jivosite.sdk.di.ui.chat.JivoChatComponent
import com.jivosite.sdk.di.ui.chat.JivoChatFragmentModule
import com.jivosite.sdk.model.SdkContext
import com.jivosite.sdk.push.JivoFirebaseMessagingService
import com.jivosite.sdk.socket.JivoWebSocketService
import com.jivosite.sdk.support.builders.Config
import com.jivosite.sdk.support.builders.ClientInfo
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

/**
 * Created on 02.09.2020.
 *
 * @author Alexandr Shibelev (av.shibelev@gmail.com)
 */
object Jivo {

    private const val TAG = "JivoSDK"

    internal lateinit var jivoSdkComponent: JivoSdkComponent
    private var serviceComponent: WebSocketServiceComponent? = null
    private var chatComponent: JivoChatComponent? = null

    private lateinit var lifecycleObserver: JivoLifecycleObserver
    private lateinit var sdkContext: SdkContext

    private var config: Config = Config.Builder().build()

    @JvmStatic
    fun init(appContext: Context, widgetId: String, host: String = "") {
        jivoSdkComponent = DaggerJivoSdkComponent.builder()
            .sdkModule(SdkModule(appContext, widgetId))
            .build()
        sdkContext = jivoSdkComponent.sdkContext()

        val storage = jivoSdkComponent.storage()

        if (host.isNotBlank()) {
            storage.host = host
        }

        val sdkConfigUseCaseProvider = jivoSdkComponent.sdkConfigUseCaseProvider()

        lifecycleObserver =
            JivoLifecycleObserver(sdkContext, storage, sdkConfigUseCaseProvider.get())
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }

    @JvmStatic
    fun setClientInfo(clientInfo: ClientInfo) {
        if (Jivo::jivoSdkComponent.isInitialized) {
            val args = bundleOf(
                "name" to clientInfo.name,
                "email" to clientInfo.email,
                "phone" to clientInfo.phone,
                "description" to clientInfo.description,
                "clientId" to jivoSdkComponent.storage().clientId
            )
            JivoWebSocketService.setClientInfo(sdkContext.appContext, args)
        }
    }

    @JvmStatic
    fun handleRemoteMessage(message: RemoteMessage): Boolean {
        return if (Jivo::jivoSdkComponent.isInitialized) {
            val handler = jivoSdkComponent.remoteMessageHandler()
            handler.handleRemoteMessage(message)
        } else {
            false
        }
    }

    @JvmStatic
    fun updatePushToken(token: String) {
        if (Jivo::jivoSdkComponent.isInitialized) {
            val useCaseProvider = jivoSdkComponent.updatePushTokenUseCaseProvider()
            useCaseProvider.get().execute(token)
        }
    }

    @JvmStatic
    fun setConfig(config: Config) {
        this.config = config
    }

    fun turnOn() {
        jivoSdkComponent.storage().let {
            if (!it.startOnInitialization) {
                it.startOnInitialization = true
                JivoWebSocketService.start(sdkContext.appContext)
            }
        }
    }

    fun turnOff() {
        jivoSdkComponent.storage().let {
            if (it.startOnInitialization) {
                it.startOnInitialization = false
                JivoWebSocketService.stop(sdkContext.appContext)
            }
        }
    }

    fun clear() {
        JivoWebSocketService.restart(sdkContext.appContext)
    }

    fun subscribeToPush() {
        if (Jivo::jivoSdkComponent.isInitialized) {
            val useCaseProvider = jivoSdkComponent.updatePushTokenUseCaseProvider()
            useCaseProvider.get().execute()
        }
    }

    fun unsubscribeFromPush() {
        if (Jivo::jivoSdkComponent.isInitialized) {
            val useCaseProvider = jivoSdkComponent.updatePushTokenUseCaseProvider()
            useCaseProvider.get().execute("")
        }
    }

    internal fun getConfig(): Config {
        return config
    }

    internal fun getServiceComponent(service: JivoWebSocketService): WebSocketServiceComponent {
        return serviceComponent ?: jivoSdkComponent.serviceComponent(
            WebSocketServiceModule(service),
            StateModule(),
            SocketMessageHandlerModule()
        ).also {
            serviceComponent = it
        }
    }

    internal fun getChatComponent(fragment: Fragment): JivoChatComponent {
        return chatComponent ?: jivoSdkComponent.chatComponent(JivoChatFragmentModule(fragment))
            .also { chatComponent = it }
    }

    internal fun clearServiceComponent() {
        serviceComponent = null
    }

    internal fun clearChatComponent() {
        chatComponent = null
    }

    internal fun d(msg: String) {
        Timber.tag(TAG).d(msg)
    }

    internal fun e(msg: String) {
        Timber.tag(TAG).e(msg)
    }

    internal fun e(e: Throwable, msg: String) {
        Timber.tag(TAG).e(e, msg)
    }

    internal fun i(msg: String) {
        Timber.tag(TAG).i(msg)
    }

    internal fun w(msg: String) {
        Timber.tag(TAG).w(msg)
    }
}