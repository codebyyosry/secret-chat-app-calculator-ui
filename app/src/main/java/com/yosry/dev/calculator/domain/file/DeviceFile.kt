package com.yosry.dev.calculator.domain.file

// domain/file/DeviceFile.kt
data class DeviceFile(
    val name: String = "",
    val path: String = "",
    val isDirectory: Boolean = false,
    val sizeBytes: Long = 0,
    val lastModified: Long = 0
)


