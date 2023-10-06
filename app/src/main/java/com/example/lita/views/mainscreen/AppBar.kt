package com.example.lita.views.mainscreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.example.lita.viewmodels.MainPageViewModel
import java.time.format.DateTimeFormatter

@Composable
fun MessageAppBar(viewModel: MainPageViewModel) {
    val onPrimaryColor = MaterialTheme.colors.onPrimary
    var menuExpanded by remember { mutableStateOf(false) }
    var infoExpanded by remember { mutableStateOf(false) }
    val infoText = remember { derivedStateOf { viewModel.info }}
    val infoColor = remember { derivedStateOf { viewModel.info.last().color ?: onPrimaryColor }}

    TopAppBar(backgroundColor = MaterialTheme.colors.primary, contentColor = Color.White) {
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(7.dp, 0.dp, 0.dp, 5.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!infoText.value.isEmpty()) {
                    Text(
                        text = infoText.value.last().message,
                        textAlign = TextAlign.Start,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = TextStyle.Default.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            color = infoColor.value
                        )
                    )
                }
            }
            if (viewModel.state.value.isEditMode) {
                IconButton(onClick = { viewModel.cancelEditMode() }) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel edit mode")
                }
            }

            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Open options"
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    offset = DpOffset(x = (-20).dp, y = 15.dp),
                    properties = PopupProperties()
                ) {
                    DropdownMenuItem(
                        onClick = {
                            menuExpanded = false
                            infoExpanded = true},
                        enabled = true
                    ) {
                        Text(
                            text = "ログを表示"
                        )
                    }
                }
            }
        }
    }
    if (infoExpanded) {
        val pattern = "M/d HH:mm:ss"
        AlertDialog(
            title = { },
            modifier = Modifier.heightIn(0.dp, 400.dp),
            onDismissRequest = { infoExpanded = false },
            text = {
                LazyColumn(){
                    items(infoText.value) { info ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val textColor = info.color ?: Color.Black
                            Text(text = info.timestamp.format(DateTimeFormatter.ofPattern(pattern)), color = textColor, modifier = Modifier.weight(1f))
                            Text(text = info.message, color = textColor, modifier = Modifier.weight(2f))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { infoExpanded = false }) {
                    Text("OK")
                }
            }
        )
    }
}
