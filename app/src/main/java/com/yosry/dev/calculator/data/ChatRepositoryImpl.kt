package com.yosry.dev.calculator.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.yosry.dev.calculator.domain.Message
import com.yosry.dev.calculator.domain.ChatRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ChatRepository {

    // Firestore paths
    private val roomRef = firestore.collection("chats").document("main_room")
    private val messagesCollection = roomRef.collection("messages")
    private val typingStatusDoc = roomRef.collection("status").document("typing")

    override fun observeMessages(): Flow<List<Message>> = callbackFlow {
        val subscription = messagesCollection
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(messages)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun sendMessage(message: Message) {
        val documentId = UUID.randomUUID().toString()
        val msgWithId = message.copy(id = documentId)
        messagesCollection.document(documentId).set(msgWithId).await()
    }

    override suspend fun deleteMessages(messageIds: List<String>) {
        firestore.runBatch { batch ->
            messageIds.forEach { id ->
                batch.delete(messagesCollection.document(id))
            }
        }.await()
    }

    override suspend fun clearAllMessages() {
        val snapshot = messagesCollection.get().await()
        firestore.runBatch { batch ->
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
        }.await()
    }

    // --- Typing Indicator Implementation ---

    override fun observeTypingStatus(otherUserCode: String): Flow<Boolean> = callbackFlow {
        val subscription = typingStatusDoc.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            // Read the boolean value for the OTHER user's code
            val isTyping = snapshot?.getBoolean(otherUserCode) ?: false
            trySend(isTyping)
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateTypingStatus(userCode: String, isTyping: Boolean) {
        val data = mapOf(userCode to isTyping)
        // Use SetOptions.merge() so we don't overwrite the other user's typing status
        typingStatusDoc.set(data, SetOptions.merge()).await()
    }
}