package com.example.lita.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.lita.data.MessagesDao
import com.example.lita.models.AddTokenRequest
import com.example.lita.models.ChatMessage
import com.example.lita.retrofit.HistoryApi
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService: FirebaseMessagingService() {
    @Inject
    lateinit var historyApi: HistoryApi

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val intent = Intent()
        intent.action = (PushNotificationBroadcastReceiver.ACTION_MESSAGE_RECEIVED)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onNewToken(token: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val response = historyApi.addToken(AddTokenRequest(token))
            if (!response.isSuccessful || response.body() == null) {
                throw Exception("Unknown Error from Retrofit")
            }
        }
        Log.d("FIREBASE", token)
    }
}

class PushNotificationBroadcastReceiver: BroadcastReceiver() {
    var onMessageReceived: (() -> Unit)? = null

    override fun onReceive(context: Context?, intent: Intent) {
        onMessageReceived?.invoke()
    }

    companion object {
        const val ACTION_MESSAGE_RECEIVED = "com.example.MESSAGE_RECEIVED"
    }
}