package com.example.lita.views.mainscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lita.R
import com.example.lita.models.Senders
import com.example.lita.ui.theme.BrightPink
import com.example.lita.viewmodels.MainPageViewModel

@Composable
fun MainScreen(viewModel: MainPageViewModel) {
    val focusRequester = FocusRequester()
    val localFocusManager = LocalFocusManager.current

    val editingBorderColor = MaterialTheme.colors.secondary
    val borderColor = remember { derivedStateOf {
        if (viewModel.state.value.isEditMode) { editingBorderColor }
        else { Color.Transparent }}}

    Scaffold(modifier = Modifier.fillMaxSize(), topBar = { MessageAppBar(viewModel)} ) {
        Box(modifier = Modifier.padding(it)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .border(width = 3.dp, color = borderColor.value), horizontalAlignment = Alignment.CenterHorizontally) {
                MessageList(viewModel = viewModel,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = viewModel.state.value.isTyping,
                            interactionSource = MutableInteractionSource(),
                            indication = null,
                            onClick = { viewModel.stopTyping(localFocusManager) }))
                BottomControl(viewModel = viewModel, modifier = Modifier, focusRequester = focusRequester, localFocusManager = localFocusManager)
            }
        }
    }
}

@Composable
fun MessageList(viewModel: MainPageViewModel, modifier: Modifier) {
    val messages = viewModel.messages
    val state = viewModel.state

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        reverseLayout = true,
        contentPadding = PaddingValues(
        vertical = 8.dp,
        horizontal = 8.dp)){
        items(messages.size) { i ->
            val message = messages[i]
            if(i >= messages.size - 1 && !state.value.endReached && !state.value.isLoading) {
                viewModel.loadNextMessages()
            }

            val arrangement = when (message.sender) {
                Senders.ME.toString() -> Arrangement.End
                Senders.WILLIS.toString() -> Arrangement.Start
                else -> Arrangement.Center
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 10.dp, 0.dp, 10.dp), horizontalArrangement = arrangement) {
                if (message.sender == Senders.SYSTEM.toString()) {
                    SystemMessageCard(message = message, modifier = Modifier)
                }
                else {
                    ChatMessageCard(message = message, viewModel = viewModel, modifier = Modifier)
                }
            }
        }
        // Show Progressbar while loading
        item{
            if(state.value.isLoading) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colors.primaryVariant)
                }
            }
        }
    }
}

@Composable
fun BottomControl(viewModel: MainPageViewModel, modifier: Modifier, focusRequester: FocusRequester, localFocusManager: FocusManager) {
    val textInput by viewModel.textInput.collectAsState()

    val normalColor = MaterialTheme.colors.primaryVariant
    val editingBackgroundColor = MaterialTheme.colors.secondary
    val editingOnBackgroundColor = MaterialTheme.colors.onSecondary

    val controlBackgroundColor = remember { derivedStateOf {
        if (viewModel.state.value.isEditMode) { editingBackgroundColor }
        else { Color.Transparent }}}
    val editingTextColor = remember { derivedStateOf {
        if (viewModel.state.value.isEditMode) {editingOnBackgroundColor }
        else {  normalColor }}}

    // Make TextField clickable
    val source = remember { MutableInteractionSource()}
    if (source.collectIsPressedAsState().value) { viewModel.startTyping() }

    Column(
        modifier
            .height(IntrinsicSize.Min)
            .background(controlBackgroundColor.value)
            .padding(0.dp, 5.dp)) {
        if (viewModel.state.value.isTyping) {
            OptionBar(viewModel = viewModel)
        }
        Row(
            modifier
                .height(IntrinsicSize.Min)
                .background(controlBackgroundColor.value)){
            OutlinedTextField(
                value = textInput,
                onValueChange = {text -> viewModel.onTextChanged(text)},
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    viewModel.stopTyping(localFocusManager)
                    viewModel.sendMessage()
                }),
                modifier = Modifier
                    .weight(1f)
                    .padding(10.dp, 5.dp, 5.dp, 5.dp)
                    .focusRequester(focusRequester),
                interactionSource = source,
                textStyle = TextStyle.Default.copy(fontSize = 18.sp),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = editingTextColor.value
                )
            )
            IconButton(onClick = {
                viewModel.stopTyping(localFocusManager)
                viewModel.sendMessage()
            },
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.CenterVertically),
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_baseline_send_24), contentDescription = "Sender change icon", tint = Color.Black)
            }
        }
    }
}

@Composable
fun OptionBar(viewModel: MainPageViewModel) {
    val normalThumbColor = MaterialTheme.colors.primary
    val editingThumbColor = BrightPink
    val thumbColor = remember { derivedStateOf {
        if (viewModel.state.value.isEditMode) { editingThumbColor } else { normalThumbColor } }
    }
    val colors = SwitchDefaults.colors(checkedThumbColor = thumbColor.value, uncheckedThumbColor = Color.LightGray)

    val senderName = remember { derivedStateOf {
        when (viewModel.state.value.sender) {
            Senders.ME -> "自分"
            Senders.WILLIS -> "ウィリス"
            else -> "システム"
        }}
    }
    var expanded by remember { mutableStateOf(false)}

    Column() {
        Row(modifier = Modifier
            .fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("送信者:")
            Box(modifier = Modifier
                .padding(5.dp)
                .clickable { expanded = true } ) {
                Text(senderName.value, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold)
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = {
                        expanded = false
                        viewModel.changeSender(Senders.ME)
                    }) { Text("自分") }
                    DropdownMenuItem(onClick = {
                        expanded = false
                        viewModel.changeSender(Senders.WILLIS)
                    }) { Text("ウィリス") }
                    DropdownMenuItem(onClick = {
                        expanded = false
                        viewModel.changeSender(Senders.SYSTEM)
                    }) { Text("システム") }
                }
            }
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            , horizontalArrangement = Arrangement.SpaceEvenly) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "会話履歴に追加", modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 5.dp))
                Switch(checked = viewModel.state.value.enabledHistory, onCheckedChange = {viewModel.toggleHistorySwitch()}, colors = colors)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("応答を生成", modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 5.dp))
                Switch(checked = viewModel.state.value.enabledGenerateResponse, onCheckedChange = { viewModel.toggleGenerateResponseSwitch() }, colors = colors)
            }
        }
    }
}
