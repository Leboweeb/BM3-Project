package com.modloader.bm3.domain.logic

import java.util.Locale

class SpeedConvert {


    companion object {

        // taken from https://stackoverflow.com/a/68822715
        fun bytesToHumanReadableSize(bytes: Float) = when {
            bytes >= 1 shl 30 -> "%.1f GB".format(bytes / (1 shl 30))
            // 1048576
            bytes >= 1 shl 20 -> "%.1f MB".format(bytes / (1 shl 20))
            // 1024
            bytes >= 1 shl 10 -> "%.0f kB".format(bytes / (1 shl 10))
            else -> "%.0f bytes".format(bytes)
        }

        fun formatETA(etaSeconds: Long): String {

            if (etaSeconds <= 0) return "Done"
            val hours = etaSeconds / 3600
            val minutes = (etaSeconds % 3600) / 60
            val seconds = etaSeconds % 60
            return when {
                hours > 0 -> String.format(Locale.US, "%dh %02dm", hours, minutes)
                minutes > 0 -> String.format(Locale.US, "%dm %02ds", minutes, seconds)
                else -> "${seconds}s"
            }
        }

    }
}