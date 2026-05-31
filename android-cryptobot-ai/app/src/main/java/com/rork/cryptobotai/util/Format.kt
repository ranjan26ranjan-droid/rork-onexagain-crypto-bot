package com.rork.cryptobotai.util

import kotlin.math.abs

/** Formats a USD amount with adaptive precision and grouping. */
fun formatUsd(value: Double, withSign: Boolean = false): String {
    val sign = when {
        withSign && value > 0 -> "+"
        value < 0 -> "-"
        else -> ""
    }
    val abs = abs(value)
    val formatted = when {
        abs >= 1_000_000_000 -> "%.2fB".format(abs / 1_000_000_000)
        abs >= 1_000_000 -> "%.2fM".format(abs / 1_000_000)
        abs >= 1 -> "%,.2f".format(abs)
        abs >= 0.01 -> "%.4f".format(abs)
        else -> "%.6f".format(abs)
    }
    return "$sign\$$formatted"
}

/** Formats a percentage with an explicit sign. */
fun formatPercent(value: Double): String {
    val sign = if (value >= 0) "+" else ""
    return "$sign%.2f%%".format(value)
}

/** Short relative time label, e.g. "2m ago". */
fun timeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> "${diff / 86_400_000}d ago"
    }
}
