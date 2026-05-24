package com.yosry.dev.calculator.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosry.dev.calculator.data.ChatRepositoryImpl
import com.yosry.dev.calculator.domain.ChatRepository
import com.yosry.dev.calculator.domain.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository = ChatRepositoryImpl()
) : ViewModel(), ChatContract.Presenter {

    private val _state = MutableStateFlow(ChatContract.State())
    override val state: StateFlow<ChatContract.State> = _state.asStateFlow()

    // ❌ REMOVED the init {} block here because we need to wait
    // for the userCode to be passed in before we start observing.

    override fun onAction(action: ChatContract.Action) {
        when (action) {
            is ChatContract.Action.Initialize -> initializeChat(action.userCode)
            is ChatContract.Action.UpdateInput -> _state.update { it.copy(inputText = action.text) }
            is ChatContract.Action.SendMessage -> sendMessage()
            is ChatContract.Action.ToggleMessageSelection -> toggleSelection(action.messageId)
            is ChatContract.Action.ClearSelection -> _state.update { it.copy(selectedMessageIds = emptySet()) }
            is ChatContract.Action.DeleteSelected -> deleteSelectedMessages()
            is ChatContract.Action.ClearChat -> clearChat()
            is ChatContract.Action.UpdateTypingStatus -> setMyTypingStatus(action.isTyping)
        }
    }

    private fun initializeChat(userCode: String) {
        // Prevent double-initialization if the view recomposes
        if (_state.value.currentUserCode == userCode) return

        _state.update { it.copy(currentUserCode = userCode) }

        // Figure out who the OTHER user is based on who logged in
        val otherUserCode = if (userCode == "200460") "280626" else "200460"

        // 1. Start observing Messages
        viewModelScope.launch {
            repository.observeMessages().collect { newMessages ->
                _state.update { it.copy(messages = newMessages) }
            }
        }

        // 2. Start observing the OTHER user's typing status
        viewModelScope.launch {
            repository.observeTypingStatus(otherUserCode).collect { isTyping ->
                _state.update { it.copy(isOtherUserTyping = isTyping) }
            }
        }
    }

    private fun sendMessage() {
        val currentState = _state.value
        if (currentState.inputText.isBlank()) return

        val message = Message(
            senderCode = currentState.currentUserCode,
            text = currentState.inputText.trim()
        )

        viewModelScope.launch {
            // Send the message
            repository.sendMessage(message)

            // Clear input and immediately set typing status to false
            _state.update { it.copy(inputText = "") }
            repository.updateTypingStatus(currentState.currentUserCode, false)
        }
    }

    private fun setMyTypingStatus(isTyping: Boolean) {
        val myCode = _state.value.currentUserCode
        if (myCode.isBlank()) return

        viewModelScope.launch {
            repository.updateTypingStatus(myCode, isTyping)
        }
    }

    private fun toggleSelection(messageId: String) {
        _state.update { state ->
            val newSelection = if (state.selectedMessageIds.contains(messageId)) {
                state.selectedMessageIds - messageId
            } else {
                state.selectedMessageIds + messageId
            }
            state.copy(selectedMessageIds = newSelection)
        }
    }

    private fun deleteSelectedMessages() {
        val idsToDelete = _state.value.selectedMessageIds.toList()
        viewModelScope.launch {
            repository.deleteMessages(idsToDelete)
            _state.update { it.copy(selectedMessageIds = emptySet()) }
        }
    }

    private fun clearChat() {
        viewModelScope.launch {
            repository.clearAllMessages()
            _state.update { it.copy(selectedMessageIds = emptySet()) }
        }
    }
}