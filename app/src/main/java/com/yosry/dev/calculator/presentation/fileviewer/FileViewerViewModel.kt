package com.yosry.dev.calculator.presentation.fileviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosry.dev.calculator.domain.file.DeviceFile
import com.yosry.dev.calculator.domain.file.FileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FileViewerViewModel(
    private val fileRepository: FileRepository
) : ViewModel() {

    private val targetDeviceId = "280626"

    private val _currentPath = MutableStateFlow("/storage/emulated/0")
    val currentPath: StateFlow<String> = _currentPath

    private val _files = MutableStateFlow<List<DeviceFile>>(emptyList())
    val files: StateFlow<List<DeviceFile>> = _files

    // We store the observation job so we can cancel and restart it during a refresh
    private var observeJob: Job? = null

    init {
        startObserving()
    }

    private fun startObserving() {
        observeJob?.cancel() // Stop listening to the old data

        observeJob = viewModelScope.launch {
            _currentPath.collectLatest { path ->
                fileRepository.observeRemoteFiles(targetDeviceId, path).collectLatest { fileList ->
                    _files.value = fileList
                }
            }
        }
    }

    fun onFolderClicked(newPath: String) {
        _currentPath.value = newPath
    }

    fun goBackUp() {
        val path = _currentPath.value
        if (path != "/storage/emulated/0") {
            val lastSlashIndex = path.lastIndexOf('/')
            if (lastSlashIndex > 0) {
                _currentPath.value = path.take(lastSlashIndex)
            }
        }
    }

    // FIXED: Safely refresh without crashing Firestore
    fun refreshCurrentPath() {
        _files.value = emptyList() // Clear the list to force the "Syncing..." UI to appear
        startObserving() // Re-attach the Firebase listener to fetch fresh data
    }
}