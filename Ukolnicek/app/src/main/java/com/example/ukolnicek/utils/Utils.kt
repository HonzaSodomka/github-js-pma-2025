package com.example.ukolnicek.utils

import android.content.Context
import android.graphics.Color

object PriorityUtils {
    // 0=Low, 1=Medium, 2=High
    fun getPriorityColor(priority: Int): Int {
        return when (priority) {
            2 -> Color.parseColor("#E57373") // Red
            1 -> Color.parseColor("#FFB74D") // Orange
            else -> Color.parseColor("#81C784") // Green
        }
    }

    fun getPriorityText(priority: Int): String {
        return when (priority) {
            2 -> "Vysoká"
            1 -> "Střední"
            else -> "Nízká"
        }
    }
}

class PreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("ukolnicek_prefs", Context.MODE_PRIVATE)

    fun saveSortOrder(orderByDeadline: Boolean) {
        prefs.edit().putBoolean("sort_by_deadline", orderByDeadline).apply()
    }

    fun getSortOrder(): Boolean {
        return prefs.getBoolean("sort_by_deadline", true) // Default true = sort by deadline
    }
}
