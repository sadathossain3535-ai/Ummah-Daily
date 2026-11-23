package com.example.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class AppUpdateInfo(
    val latestVersionCode: Int,
    val latestVersionName: String,
    val titleBn: String,
    val messageBn: String,
    val updateUrl: String,
    val forceUpdate: Boolean
)
