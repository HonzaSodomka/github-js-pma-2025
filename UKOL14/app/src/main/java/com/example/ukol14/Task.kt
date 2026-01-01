package com.example.ukol14

data class Task(
    val id: String = "",           // Firestore ID
    val title: String = "",         // Název úkolu
    val isCompleted: Boolean = false  // Je hotový?
)