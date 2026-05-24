package com.yosry.dev.calculator.presentation.chat

// ChatContract.kt
import com.yosry.dev.calculator.domain.Message
import kotlinx.coroutines.flow.StateFlow

interface ChatContract {
    data class State(
        val messages: List<Message> = emptyList(),
        val selectedMessageIds: Set<String> = emptySet(),
        val currentUserCode: String = "",
        val inputText: String = "",
        val isOtherUserTyping: Boolean = false // Added for typing indicator
    )

    sealed class Action {
        data class Initialize(val userCode: String) : Action()
        data class UpdateInput(val text: String) : Action()
        object SendMessage : Action()
        data class ToggleMessageSelection(val messageId: String) : Action()
        object ClearSelection : Action()
        object DeleteSelected : Action()
        object ClearChat : Action()
        data class UpdateTypingStatus(val isTyping: Boolean) : Action()
    }

    interface Presenter {
        val state: StateFlow<State>
        fun onAction(action: Action)
    }
}
