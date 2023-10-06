package com.example.lita.viewmodels

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteException
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lita.MainPageRepository
import com.example.lita.data.Converters
import com.example.lita.helpers.paginator.DefaultPaginator
import com.example.lita.models.ChatMessage
import com.example.lita.models.ChatMessageView
import com.example.lita.models.Senders
import com.example.lita.models.toChatMessageView
import com.example.lita.ui.theme.LightGreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MainPageViewModel @Inject constructor(
    application: Application,
    private val repository: MainPageRepository
): AndroidViewModel(application) {
    private val _messages = mutableStateListOf<ChatMessageView>()
    private val _textInput = MutableStateFlow("")
    private val _info = mutableStateListOf<InfoObject>()
    private val _state = mutableStateOf(MainScreenState())
    var preservedHistoryState: Boolean? = null

    private val paginator = getPaginator()
    private val pagingBatchSize = 10

    val messages: MutableList<ChatMessageView> = _messages
    val state: State<MainScreenState> = _state
    val textInput: StateFlow<String> = _textInput
    val info: MutableList<InfoObject> = _info

    fun onTextChanged(text: String) {
        _textInput.value = text
    }

    fun loadNextMessages() {
        viewModelScope.launch {
            var errorMessage = ""
            // Get messages from remote server and update local database
            try {
                if (!isInternetAvailable()) {
                    errorMessage = "インターネットに接続できません。"
                }
                else {
                    // Get the timestamp of the last message
                    val lastTimestamp = _messages.lastOrNull()?.timestamp
                    if (lastTimestamp != null) {
                        repository.getMessagesFromServer(Converters().toString(lastTimestamp))
                    }
                    else {
                        repository.getMessagesFromServer()
                    }
                }
            } catch (ex: Exception) {
                when (ex) {
                    is IOException -> {
                        errorMessage = "ネットワークに接続できません。Error: " + ex.message
                    }
                    else -> {
                        errorMessage = "サーバーの接続中にエラーが発生しました。 Error: " + ex.message
                    }
                }
            }
            // Get messages from local database and update UI
            try {
                addLogMessage("データを取得しています...")
                paginator.loadNextItem()
            } catch (ex: Exception) {
                when (ex) {
                    is SQLiteException, is IllegalStateException, is SQLiteConstraintException, is SQLiteDiskIOException -> {
                        addLogMessage("ローカルデータベースへのアクセスに失敗しました。Error: " + ex.message, Color.Red)
                    }
                    else -> throw ex
                }
            }
            if (errorMessage.length > 0) {
                addLogMessage(errorMessage, Color.Red)
            } else {
                addLogMessage("✔ 準備完了", LightGreen)
            }
        }
    }

    fun checkForNewMessages() {
        if (_messages.isEmpty()) {
            loadNextMessages()
        } else {
            viewModelScope.launch {
                if (!isInternetAvailable()) {
                    addLogMessage("インターネットに接続できません。", Color.Red)
                    return@launch
                }

                try {
                    addLogMessage("サーバーからデータを取得しています...")
                    repository.getMessagesFromServer()
                } catch (ex: Exception) {
                    addLogMessage("サーバーの接続中にエラーが発生しました。Error: " + ex.message, Color.Red)
                }
                try {
                    addLogMessage("更新しています...")
                    val retrievedMessages =
                        repository.getMessages(0, pagingBatchSize).map { it.toChatMessageView() }
                    retrievedMessages.forEach { newMessage ->
                        val existingMessage = _messages.find { it.id == newMessage.id }
                        existingMessage.let {
                            _messages.remove(existingMessage)
                        }
                        _messages.add(newMessage)
                    }
                    _messages.sortByDescending { it.timestamp }
                    addLogMessage("新しいメッセージを受信しました。", LightGreen)
                } catch (ex: Exception) {
                    when (ex) {
                        is SQLiteException, is IllegalStateException, is SQLiteConstraintException, is SQLiteDiskIOException -> {
                            addLogMessage("ローカルデータベースへのアクセスに失敗しました。Error: " + ex.message, Color.Red)
                        }
                        else -> throw ex
                    }
                }
            }
        }
    }

    fun sendMessage() {
        if (_textInput.value.isEmpty()) return
        if (!isInternetAvailable())  {
            addLogMessage("インターネットに接続できません。", Color.Red)
            return
        }

        if (_state.value.isEditMode) {
            _messages.firstOrNull { it.id == _state.value.editingId }.let { updatedMessage ->
                viewModelScope.launch {
                    try {
                        addLogMessage("メッセージを修正しています...")
                        val returnedMessage = repository.updateMessage(
                            id = updatedMessage!!.id,
                            newMessage = _textInput.value,
                            sender = _state.value.sender,
                            isHistoryEnabled = _state.value.enabledHistory,
                            isGenerateResponseEnabled = _state.value.enabledGenerateResponse
                        )
                        _messages[_messages.indexOf(updatedMessage)] = returnedMessage.toChatMessageView()
                        addLogMessage("メッセージを修正しました。", LightGreen)
                    } catch (ex: Exception) {
                        when (ex) {
                            is SQLiteException, is IllegalStateException, is SQLiteConstraintException, is SQLiteDiskIOException -> {
                                addLogMessage("ローカルデータベースへのアクセスに失敗しました。Error: " + ex.message, Color.Red)
                            }
                            else -> throw ex
                        }
                    }
                }
            }
        }
        else {
            viewModelScope.launch {
                try {
                    addLogMessage("メッセージを送信しています...")
                    val newMessage = repository.sendMessage(
                        textInput =  _textInput.value,
                        sender = _state.value.sender,
                        isHistoryEnabled = _state.value.enabledHistory,
                        isGenerateResponseEnabled = _state.value.enabledGenerateResponse)
                    _messages.add(0, newMessage.toChatMessageView())
                    addLogMessage("メッセージを送信しました。", LightGreen)
                } catch (ex: Exception) {
                    when (ex) {
                        is SQLiteException, is IllegalStateException, is SQLiteConstraintException, is SQLiteDiskIOException -> {
                            addLogMessage("ローカルデータベースへのアクセスに失敗しました。Error: " + ex.message, Color.Red)
                        }
                        else -> throw ex
                    }
                }
            }
        }
        cancelEditMode()
    }

    fun onMessageReceived() {
        checkForNewMessages()
    }

    fun deleteMessage(message: ChatMessageView) {
        viewModelScope.launch {
            if (!isInternetAvailable()) {
                addLogMessage("インターネットに接続できないため削除できません。")
                return@launch
            }

            try {
                addLogMessage("メッセージを削除しています...")
                repository.deleteMessage(message.id, Converters().toString(message.timestamp))
                _messages.remove(message)
                addLogMessage("メッセージを削除しました。", LightGreen)
            } catch (ex: Exception) {
                when (ex) {
                    is SQLiteException, is IllegalStateException, is SQLiteConstraintException, is SQLiteDiskIOException -> {
                        addLogMessage("ローカルデータベースへのアクセスに失敗しました。Error: " + ex.message, Color.Red)
                    }
                    else -> throw ex
                }
            }
        }
    }

    private fun getPaginator(): DefaultPaginator<Int, ChatMessage> {
        return DefaultPaginator(
            initialKey = _state.value.position,
            onLoadUpdated = { _state.value = _state.value.copy(isLoading = it)},
            onRequest = { nextPage -> repository.getMessages(nextPage, pagingBatchSize)},
            getNextKey = { _state.value.position + pagingBatchSize },
            onError = { _state.value = _state.value.copy(loadingError = it?.localizedMessage)},
            onSuccess = {items, newKey ->
                val messages = items.map { it.toChatMessageView() }
                _messages += messages
                _state.value = _state.value.copy(
                    position = newKey,
                    endReached = messages.isEmpty()
                )
            }
        )
    }

    fun startTyping() {
        _state.value = _state.value.copy(isTyping = true)
    }

    fun stopTyping(localFocusManager: FocusManager) {
        localFocusManager.clearFocus()
        _state.value = _state.value.copy(isTyping = false)
    }

    fun startEditMode(message: ChatMessageView) {
        preservedHistoryState = _state.value.enabledHistory     // Preserve the current mode
        _state.value = _state.value.copy(isEditMode = true, editingId = message.id,  sender = Senders.fromName(message.sender)!!, enabledHistory = message.isHistoryEnabled)
        _textInput.value = message.message
    }

    fun cancelEditMode() {
        _state.value = _state.value.copy(isEditMode = false, editingId = "", sender = Senders.ME)
        // Restore history mode
        preservedHistoryState?.let {
            _state.value = _state.value.copy(enabledHistory = it) }
        preservedHistoryState = null

        _textInput.value = ""
    }

    fun toggleHistorySwitch() {
        _state.value = _state.value.copy(enabledHistory = !_state.value.enabledHistory)
    }

    fun toggleGenerateResponseSwitch() {
        _state.value = _state.value.copy(enabledGenerateResponse = !_state.value.enabledGenerateResponse)
    }

    fun changeSender(sender: Senders) {
        _state.value = _state.value.copy(sender = sender)
    }

    private fun addLogMessage(message: String, color: Color? = null) {
        val ldt = LocalDateTime.now()
        val newInfo = InfoObject(message, color, ldt)
        _info.add(newInfo)
    }

    private fun isInternetAvailable(): Boolean {
        val context = getApplication<Application>().applicationContext
        var result = false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                result = when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }

            }
        }
        return result
    }
}

data class MainScreenState(
    val position: Int = 0,
    val isLoading: Boolean = false,
    val loadingError: String? = null,
    val endReached: Boolean = false,
    val isTyping: Boolean = false,
    val isEditMode: Boolean = false,
    val editingId: String = "",
    val sender: Senders = Senders.ME,
    val enabledHistory: Boolean = true,
    val enabledGenerateResponse: Boolean = true
)

data class InfoObject(
    val message: String,
    val color: Color?,
    val timestamp: LocalDateTime
)