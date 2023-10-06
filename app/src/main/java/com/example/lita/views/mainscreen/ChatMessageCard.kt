package com.example.lita.views.mainscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.example.lita.helpers.Utilities
import com.example.lita.models.ChatMessageView
import com.example.lita.models.Senders
import com.example.lita.viewmodels.MainPageViewModel

@Composable
fun ChatMessageCard(message: ChatMessageView, viewModel: MainPageViewModel, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var isAlertShowing by remember { mutableStateOf(false) }

    val normalColor = MaterialTheme.colors.primaryVariant
    val editingBackgroundColor = MaterialTheme.colors.secondary

    val cardColor = if (viewModel.state.value.isEditMode && viewModel.state.value.editingId == message.id) { editingBackgroundColor }
                    else if (message.sender == Senders.ME.toString()){ normalColor }
                    else { Color.White }
    val alignment = if (message.sender == Senders.ME.toString()) { Alignment.End} else { Alignment.Start }
    val textAlignment = if (message.sender == Senders.ME.toString()) { TextAlign.End } else { TextAlign.Start }

    Column(modifier = modifier, horizontalAlignment = alignment){
        Card(elevation = 4.dp, modifier = modifier
            .padding(10.dp, 10.dp, 10.dp, 5.dp)
            .widthIn(60.dp, 250.dp)
            .clickable(onClick = { expanded = true }),
            shape = RoundedCornerShape(10),
            backgroundColor = cardColor) {
            Text(text = message.message, modifier = Modifier.padding(15.dp), color = MaterialTheme.colors.onSurface)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, properties = PopupProperties()) {
            DropdownMenuItem(onClick = {
                viewModel.startEditMode(message)
                expanded = false
            }) {
                Text("編集")
            }
            DropdownMenuItem(onClick = { isAlertShowing = true }) {
                Text("削除")
            }
        }
        DatetimeMessage(message = Utilities.localDateTimeToString(message.timestamp), align = textAlignment, modifier = Modifier.padding(10.dp,0.dp,10.dp, 0.dp))

        if (isAlertShowing) {
            AlertDialog(
                onDismissRequest = { isAlertShowing = false },
                title = { Text(text = "メッセージの削除") },
                text = { Text(text = "メッセージを削除します。") },
                confirmButton = {
                    TextButton(onClick = {
                        isAlertShowing = false
                        expanded = false
                        viewModel.deleteMessage(message)
                    }) {
                        Text(text = "OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        isAlertShowing = false
                        expanded = false
                    }) {
                        Text(text = "キャンセル")
                    }
                })
        }
    }
}

@Composable
fun SystemMessageCard(message: ChatMessageView, modifier : Modifier) {
    Column(modifier = modifier.widthIn(0.dp, 250.dp), horizontalAlignment = Alignment.CenterHorizontally){
        Card(elevation = 4.dp, modifier = Modifier.padding(10.dp, 10.dp, 10.dp, 2.dp),
            shape = RoundedCornerShape(50),
            backgroundColor = Color.LightGray) {
            Text(text = message.message, modifier = Modifier.padding(15.dp))
        }
        DatetimeMessage(message = Utilities.localDateTimeToString(message.timestamp), align = TextAlign.Center, modifier = Modifier.padding(10.dp,0.dp,10.dp, 5.dp))
    }
}

@Composable
fun DatetimeMessage(message: String, align: TextAlign, modifier: Modifier) {
    Text(text = message, style = MaterialTheme.typography.caption,
        textAlign = align, modifier = modifier)
}

