package com.yosry.dev.calculator.domain.file
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    // For the Client: Reads local Android files and uploads the list to Firebase
    suspend fun syncLocalFolderToFirebase(deviceId: String, folderPath: String)

    // For the Admin: Listens to Firebase to see the remote device's files
    fun observeRemoteFiles(deviceId: String, folderPath: String): Flow<List<DeviceFile>>
}