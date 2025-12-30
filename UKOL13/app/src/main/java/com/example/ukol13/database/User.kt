package com.example.ukol13.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val totalScore: Int = 0,
    val gamesPlayed: Int = 0
)