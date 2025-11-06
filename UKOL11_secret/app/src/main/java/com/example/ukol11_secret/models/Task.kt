package com.example.ukol11_secret.models

data class Task(
    val id: String,
    var title: String,
    var description: String,
    var category: String,
    var priority: String,
    var deadline: String,
    var isCompleted: Boolean = false,
    var imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)