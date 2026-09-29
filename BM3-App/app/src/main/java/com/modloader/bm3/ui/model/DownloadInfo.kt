package com.modloader.bm3.ui.model

import com.modloader.bm3.utils.DownloadStatus

/**
 * @property speed speed of download in bytes/sec, it is the caller's responsibility to convert to kb/s,mb/s, or other common formats
 * @property timeRemaining time left for download to finish in **seconds**
 * @property progress progress of download as integer from 0 till 100 representing the percentage of completion of the download
 *  */
data class DownloadInfo(
    val id: Int,
    val url: String,
    val title: String,
    val speed: Float,
    val timeRemaining: Long,
    val progress: Int,
    val state: DownloadStatus,
    val failureReason: String,
)