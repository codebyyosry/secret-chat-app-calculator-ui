package com.yosry.dev.calculator.presentation.fileviewer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Refresh // Added Import
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.yosry.dev.calculator.data.file.FileRepositoryImpl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFileViewerScreen(
    deviceId: String,
    onExit: () -> Unit
) {
    val factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val firestore = FirebaseFirestore.getInstance()
            val repository = FileRepositoryImpl(firestore)
            return FileViewerViewModel(repository) as T
        }
    }

    val viewModel: FileViewerViewModel = viewModel(factory = factory)

    val remoteFiles by viewModel.files.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Device Files", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(currentPath, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentPath == "/storage/emulated/0") {
                            onExit()
                        } else {
                            viewModel.goBackUp()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                // NEW: Added Refresh Button here
                actions = {
                    IconButton(onClick = { viewModel.refreshCurrentPath() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->

        // Use currentPath.isEmpty() check to show a quick loading state during refresh
        if (remoteFiles.isEmpty() || currentPath.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Folder is empty or syncing...", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                items(remoteFiles, key = { it.path }) { fileInfo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (fileInfo.isDirectory) {
                                    viewModel.onFolderClicked(fileInfo.path)
                                } else {
                                    // Handle file click
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (fileInfo.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            tint = if (fileInfo.isDirectory) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(text = fileInfo.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                            if (!fileInfo.isDirectory) {
                                Text(
                                    text = "${fileInfo.sizeBytes / 1024} KB",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}