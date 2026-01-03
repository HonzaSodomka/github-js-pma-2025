package com.example.fitnesstracker.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

// 1. Nejmenší jednotka: Jedna série (např. 100kg na 5 opakování)
@Parcelize
data class WorkoutSet(
    var weight: Double = 0.0,
    var reps: Int = 0,
    var isCompleted: Boolean = false // Hodí se pro odškrtávání během tréninku
) : Parcelable

// 2. Prostřední jednotka: Cvik (např. Bench Press, který má 3 série)
@Parcelize
data class WorkoutExercise(
    var name: String = "",
    var sets: ArrayList<WorkoutSet> = ArrayList()
) : Parcelable

// 3. Hlavní jednotka: Celý trénink (např. "Pondělí - Prsa")
@Parcelize
data class Workout(
    var id: String = "",           // ID z Firestore
    var name: String = "",         // Název tréninku
    var date: Date = Date(),       // Kdy se to stalo
    var durationSeconds: Long = 0, // Jak dlouho trval
    var exercises: ArrayList<WorkoutExercise> = ArrayList()
) : Parcelable
