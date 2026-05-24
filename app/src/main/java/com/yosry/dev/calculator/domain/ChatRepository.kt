package com.yosry.dev.calculator.domain



// ChatRepository.kt
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(): Flow<List<Message>>
    suspend fun sendMessage(message: Message)
    suspend fun deleteMessages(messageIds: List<String>)
    suspend fun clearAllMessages()

    // Typing Indicator Operations
    fun observeTypingStatus(otherUserCode: String): Flow<Boolean>
    suspend fun updateTypingStatus(userCode: String, isTyping: Boolean)
}