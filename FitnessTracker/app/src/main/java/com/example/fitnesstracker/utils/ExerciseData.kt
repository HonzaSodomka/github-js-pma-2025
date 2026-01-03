package com.example.fitnesstracker.utils

object ExerciseData {
    // Statická mapa základních cviků
    val categories = mapOf(
        "Hrudník" to listOf("Bench Press", "Incline Bench Press", "Kliky", "Rozpažky", "Peck Deck", "Tlaky na stroji"),
        "Záda" to listOf("Mrtvý tah", "Shyby", "Přítahy v předklonu", "Stahování kladky", "Hyperextenze", "Veslování"),
        "Nohy" to listOf("Dřep", "Leg Press", "Výpady", "Předkopávání", "Zakopávání", "Výpony na lýtka", "Hacken Dřep"),
        "Ramena" to listOf("Military Press", "Tlaky s jednoručkami", "Upažování", "Předpažování", "Zadní delty"),
        "Biceps" to listOf("Bicepsový zdvih (osa)", "Kladivové zdvihy", "Zdvihy na Scottově lavici", "Koncentrovaný zdvih"),
        "Triceps" to listOf("Tricepsové stahování kladky", "Francouzský tlak", "Bradla (Dips)", "Kick-back"),
        "Břicho" to listOf("Sklapovačky", "Plank", "Zvedání nohou ve visu", "Russian Twist", "Zkracovačky"),
        "Kardio" to listOf("Běh (pás)", "Kolo", "Eliptický trenažér", "Veslování (trenažér)", "Skákání přes švihadlo")
    )

    // Funkce, která zjistí kategorii podle názvu cviku (pro automatické pojmenování tréninku)
    fun getCategoryForExercise(exerciseName: String, customExercises: List<CustomExercise> = emptyList()): String {
        // 1. Hledáme v základních cvicích
        for ((category, exercises) in categories) {
            if (exercises.contains(exerciseName)) {
                return category
            }
        }
        // 2. Hledáme ve vlastních cvicích
        val foundCustom = customExercises.find { it.name == exerciseName }
        if (foundCustom != null) {
            return foundCustom.category
        }

        return "Ostatní"
    }
}

// Jednoduchá třída pro vlastní cviky (abychom ji mohli používat v Utils i v Aktivitě)
data class CustomExercise(val name: String = "", val category: String = "")
