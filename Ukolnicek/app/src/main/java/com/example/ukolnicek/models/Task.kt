package com.example.ukolnicek.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var title: String = "",
    var description: String = "",
    var priority: Int = 1, // 0=Low, 1=Medium, 2=High
    var difficulty: Int = 1, // 1-5 scale
    var deadlineTimestamp: Long = 0,
    var isCompleted: Boolean = false,
    var userId: String = "" // For Firebase sync
) : Parcelable
