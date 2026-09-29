package com.modloader.bm3.utils

enum class DownloadStatus {
    QUEUED,

    STARTED,

    PROGRESS,

    SUCCESS,

    CANCELLED,

    FAILED,

    PAUSED,

    DEFAULT;

    companion object {
        fun fromInt(value: Int): DownloadStatus = entries.first { it.ordinal == value }
    }
}