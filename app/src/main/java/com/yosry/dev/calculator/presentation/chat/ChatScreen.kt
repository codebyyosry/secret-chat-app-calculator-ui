package com.yosry.dev.calculator.presentation.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yosry.dev.calculator.R
import com.yosry.dev.calculator.domain.Message
import com.yosry.dev.calculator.presentation.utils.getMessageDateCategory
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- User Mapping Helpers ---
fun getUserName(code: String): String = when (code) {
    "200460" -> "Dark"
    "280626" -> "Moon"
    else -> "Unknown"
}

fun getUserAvatar(code: String): Int = when (code) {
    "200460" -> R.drawable.ic_yosry
    "280626" -> R.drawable.ic_aprilyn
    else -> R.drawable.ic_outline_account_circle // Replace with a default avatar if needed
}

fun getOtherUserCode(myCode: String): String = if (myCode == "200460") "280626" else "200460"
// ----------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userCode: String,
    presenter: ChatContract.Presenter = viewModel<ChatViewModel>(),
    onBack: () -> Unit
) {
    val state by presenter.state.collectAsState()
    var showMenu by remember { mutableStateOf(false) }



    LaunchedEffect(userCode) {
        presenter.onAction(ChatContract.Action.Initialize(userCode))
    }

    val isSelectionMode = state.selectedMessageIds.isNotEmpty()
    val otherUserCode = getOtherUserCode(state.currentUserCode)

    Scaffold(topBar = {
        TopAppBar(
            title = {
                if (isSelectionMode) {
                    Text("${state.selectedMessageIds.size} Selected")
                } else {
                    // Custom Toolbar showing the OTHER user's avatar and name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(code = otherUserCode, size = 36)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = getUserName(otherUserCode),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }, navigationIcon = {
                if (isSelectionMode) {
                    IconButton(onClick = { presenter.onAction(ChatContract.Action.ClearSelection) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close selection")
                    }
                } else {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            },
            actions = {
                if (isSelectionMode) {
                    IconButton(onClick = { presenter.onAction(ChatContract.Action.DeleteSelected) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete selected")
                    }
                } else {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = {
                            showMenu = false
                        } // 👈 Fix 1: Set to false to close when tapping outside
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear Chat") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Clear, // 👈 Here is the built-in Clear icon
                                    contentDescription = "Clear Chat"
                                )
                            },
                            onClick = {
                                showMenu =
                                    false // 👈 Fix 2: Set to false to close after clicking the item
                                presenter.onAction(ChatContract.Action.ClearChat)
                            }
                        )
                    }
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (isSelectionMode) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                titleContentColor = if (isSelectionMode) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = if (isSelectionMode) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = if (isSelectionMode) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
            )
        )
    }, bottomBar = {
        ChatInputBar(
            inputText = state.inputText,
            onInputChanged = {
                presenter.onAction(ChatContract.Action.UpdateInput(it))
            },
            onSend = { presenter.onAction(ChatContract.Action.SendMessage) },
            onTypingStatusChange = { isTyping ->
                presenter.onAction(ChatContract.Action.UpdateTypingStatus(isTyping))
            })
    }) { paddingValues ->
        val listState = rememberLazyListState()

        // 1. Group messages by their date category efficiently
        val groupedMessages = remember(state.messages) {
            state.messages.groupBy { getMessageDateCategory(it.timestamp) }
        }
        // Auto-scroll to bottom
        LaunchedEffect(state.messages.size, state.isOtherUserTyping) {
            if (state.messages.isNotEmpty()) {
                // Total items = number of headers + number of messages + typing indicator
                val totalItems = groupedMessages.size + state.messages.size + if (state.isOtherUserTyping) 1 else 0
                listState.animateScrollToItem(totalItems - 1)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp)
        ) {

            // 3. Iterate through the grouped map
            groupedMessages.forEach { (dateCategory, messagesForDate) ->

                // Add the Date Header
                item(key = dateCategory) {
                    DateHeader(text = dateCategory)
                }

                // Add the messages for that specific date
                items(messagesForDate, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        isMine = message.senderCode == state.currentUserCode,
                        isSelected = state.selectedMessageIds.contains(message.id),
                        selectionModeActive = isSelectionMode,
                        onClick = {
                            if (isSelectionMode) presenter.onAction(ChatContract.Action.ToggleMessageSelection(message.id))
                        },
                        onLongClick = {
                            presenter.onAction(ChatContract.Action.ToggleMessageSelection(message.id))
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Typing Indicator remains at the very bottom
            if (state.isOtherUserTyping) {
                item {
                    TypingBubble(otherUserCode = otherUserCode)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}


@Composable
fun DateHeader(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun UserAvatar(code: String, size: Int = 32) {
    Image(
        painter = painterResource(id = getUserAvatar(code)),
        contentDescription = "${getUserName(code)} Avatar",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isMine: Boolean,
    isSelected: Boolean,
    selectionModeActive: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        isMine -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onSurface
        isMine -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isMine) 16.dp else 4.dp,
        bottomEnd = if (isMine) 4.dp else 16.dp
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
            .combinedClickable(
                onClick = { if (selectionModeActive) onClick() }, onLongClick = onLongClick
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMine) {
            UserAvatar(code = message.senderCode)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(backgroundColor)
                .padding(12.dp)
        ) {
            Text(text = message.text, color = textColor, fontSize = 16.sp)
            Text(
                text = formatTimestamp(message.timestamp),
                color = textColor.copy(alpha = 0.7f),
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp)
            )
        }

        if (isMine) {
            Spacer(modifier = Modifier.width(8.dp))
            UserAvatar(code = message.senderCode)
        }
    }
}

@Composable
fun TypingBubble(otherUserCode: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        UserAvatar(code = otherUserCode)
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp
                    )
                )
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            TypingDotsIndicator()
        }
    }
}

@Composable
fun TypingDotsIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    @Composable
    fun animateDot(delayMillis: Int): State<Float> = infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.0f at delayMillis with LinearEasing
                1.0f at delayMillis + 300 with LinearEasing
                0.0f at delayMillis + 600 with LinearEasing
            }, repeatMode = RepeatMode.Restart
        ), label = "dot_$delayMillis"
    )

    val dot1 by animateDot(0)
    val dot2 by animateDot(200)
    val dot3 by animateDot(400)

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Dot(dot1)
        Dot(dot2)
        Dot(dot3)
    }
}

@Composable
fun Dot(alpha: Float) {
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f + (alpha * 0.7f)))
    )
}

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    onTypingStatusChange: (Boolean) -> Unit
) {
    // 1. Get the keyboard controller
    val keyboardController = LocalSoftwareKeyboardController.current

    // Automatically stop typing indicator 2 seconds after the user stops typing
    LaunchedEffect(inputText) {
        if (inputText.isNotBlank()) {
            onTypingStatusChange(true)
            delay(2000)
            onTypingStatusChange(false)
        } else {
            onTypingStatusChange(false)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChanged,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message...") },
            shape = RoundedCornerShape(24.dp),
            maxLines = 4,
            // 2. Setup the keyboard to show a "Send" action instead of standard enter
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    onSend()
                    keyboardController?.hide() // Hide when pressing send on the keyboard itself
                })
        )
        Spacer(modifier = Modifier.width(8.dp))
        FloatingActionButton(
            onClick = {
                onSend()
                keyboardController?.hide() // 3. Hide when clicking your physical UI button
            },
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
            shape = CircleShape
        ) {
            Icon(
                Icons.AutoMirrored.Default.Send,
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

}




fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}