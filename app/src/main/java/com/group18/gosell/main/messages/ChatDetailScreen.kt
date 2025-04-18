package com.group18.gosell.main.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.group18.gosell.data.model.Message
import com.group18.gosell.ui.theme.GoSellColorSecondary
import com.group18.gosell.ui.theme.GoSellTextSecondary
import com.group18.gosell.ui.theme.GosellTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    navController: NavController,
    chatId: String?,
    otherUserId: String?,
    viewModel: ChatDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val otherUserName = otherUserId?.let { uiState.chatInfo?.participantNames?.get(it) } ?: "User"
    val chatTitle = if (!uiState.chatInfo?.productName.isNullOrBlank()) {
        "$otherUserName - ${uiState.chatInfo?.productName}"
    } else {
        otherUserName
    }

    LaunchedEffect(chatId, otherUserId) {
        if (!chatId.isNullOrBlank()) {
            viewModel.initializeChat(chatId, otherUserId)
        } else {
            println("Error: Invalid chatId received in ChatDetailScreen")
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    GosellTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(chatTitle, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                MessageInput(
                    message = uiState.messageToSend,
                    onMessageChange = { viewModel.updateMessageToSend(it) },
                    onSendClick = {
                        viewModel.sendMessage()
                        keyboardController?.hide()
                    },
                    enabled = !uiState.isLoading
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                when {
                    uiState.isLoading && uiState.messages.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    uiState.error != null -> {
                        Text(
                            text = uiState.error ?: "An error occurred",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center).padding(16.dp)
                        )
                    }
                    uiState.messages.isEmpty() && !uiState.isLoading -> {
                        Text(
                            "Start the conversation!",
                            modifier = Modifier.align(Alignment.Center).padding(16.dp),
                            color = GoSellTextSecondary
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.messages, key = { it.id }) { message ->
                                MessageBubble(
                                    message = message,
                                    isCurrentUserSender = message.senderId == currentUserId
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isCurrentUserSender: Boolean) {
    val alignment = if (isCurrentUserSender) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isCurrentUserSender) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val textColor = if (isCurrentUserSender) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isCurrentUserSender) 16.dp else 0.dp,
        bottomEnd = if (isCurrentUserSender) 0.dp else 16.dp
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isCurrentUserSender) 40.dp else 0.dp,
                end = if (isCurrentUserSender) 0.dp else 40.dp
            ),
        contentAlignment = alignment
    ) {
        Column(horizontalAlignment = if (isCurrentUserSender) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(backgroundColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 15.sp
                )
            }
            Text(
                text = formatTimestampTimeOnly(message.timestamp),
                fontSize = 10.sp,
                color = GoSellTextSecondary,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}


@Composable
fun MessageInput(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { onSendClick() }
                ),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = GoSellColorSecondary.copy(alpha=0.5f),
                    unfocusedContainerColor = GoSellColorSecondary.copy(alpha=0.5f),
                    disabledContainerColor = GoSellColorSecondary.copy(alpha = 0.3f),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSendClick,
                enabled = message.isNotBlank() && enabled,
                modifier = Modifier.size(48.dp)
                    .clip(CircleShape)
                    .background(if (message.isNotBlank() && enabled) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Message",
                    tint = if (message.isNotBlank() && enabled) MaterialTheme.colorScheme.onPrimary else Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

fun formatTimestampTimeOnly(date: Date?): String {
    if (date == null) return ""
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
}