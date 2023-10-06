package com.example.lita

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.lita.services.PushNotificationBroadcastReceiver
import com.example.lita.ui.theme.LITATheme
import com.example.lita.viewmodels.MainPageViewModel
import com.example.lita.views.mainscreen.MainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainPageViewModel by viewModels()
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "通知を許可しました。", Toast.LENGTH_SHORT).show()
        }
    }
    private val receiver = PushNotificationBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()
        setContent {
            LITATheme {
                MainScreen(viewModel)
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onResume() {
        val viewModel: MainPageViewModel by viewModels()
        super.onResume()
        registerLocalBroadcast(viewModel)
        viewModel.checkForNewMessages()
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    private fun registerLocalBroadcast(viewModel: MainPageViewModel) {
        receiver.onMessageReceived = {
            viewModel.onMessageReceived()
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(PushNotificationBroadcastReceiver.ACTION_MESSAGE_RECEIVED))
    }
}
