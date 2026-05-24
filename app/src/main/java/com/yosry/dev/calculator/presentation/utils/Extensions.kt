package com.yosry.dev.calculator.presentation.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun getMessageDateCategory(timestamp: Long): String {
    val messageCalendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    val currentCalendar = Calendar.getInstance()

    // Check if Today
    if (messageCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
        messageCalendar.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR)
    ) {
        return "Today"
    }

    // Check if Yesterday
    currentCalendar.add(Calendar.DAY_OF_YEAR, -1)
    if (messageCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
        messageCalendar.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR)
    ) {
        return "Yesterday"
    }

    // Otherwise format as "Month DD, YYYY" (e.g., "May 24, 2026")
    val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}