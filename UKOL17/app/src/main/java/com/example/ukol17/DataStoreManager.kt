package com.example.ukol17

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "christmas_app")

class DataStoreManager(private val context: Context) {

    companion object {
        private val CHRISTMAS_DATE = stringPreferencesKey("christmas_date")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val WISHES = stringSetPreferencesKey("wishes")
        private val PRIORITY_WISHES = stringSetPreferencesKey("priority_wishes")
        private val COMPLETED_WISHES = stringSetPreferencesKey("completed_wishes")

        private val OPENED_DAYS = stringSetPreferencesKey("opened_days")
        private val COMPLETED_DAYS = stringSetPreferencesKey("completed_days")
    }

    // Načíst datum Vánoc
    val christmasDate: Flow<String> = context.dataStore.data
        .map { it[CHRISTMAS_DATE] ?: "2025-12-24" }

    // Načíst jméno
    val userName: Flow<String> = context.dataStore.data
        .map { it[USER_NAME] ?: "" }

    // Načíst všechna přání
    val wishes: Flow<Set<String>> = context.dataStore.data
        .map { it[WISHES] ?: emptySet() }

    // Načíst prioritní přání
    val priorityWishes: Flow<Set<String>> = context.dataStore.data
        .map { it[PRIORITY_WISHES] ?: emptySet() }

    // Načíst splněná přání
    val completedWishes: Flow<Set<String>> = context.dataStore.data
        .map { it[COMPLETED_WISHES] ?: emptySet() }

    // Otevřené dny kalendáře
    val openedDays: Flow<Set<String>> = context.dataStore.data
        .map { it[OPENED_DAYS] ?: emptySet() }

    // Splněné dny kalendáře
    val completedDays: Flow<Set<String>> = context.dataStore.data
        .map { it[COMPLETED_DAYS] ?: emptySet() }

    // Uložit datum Vánoc
    suspend fun saveChristmasDate(date: String) {
        context.dataStore.edit { it[CHRISTMAS_DATE] = date }
    }

    // Uložit jméno
    suspend fun saveUserName(name: String) {
        context.dataStore.edit { it[USER_NAME] = name }
    }

    // Přidat přání
    suspend fun addWish(wish: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[WISHES]?.toMutableSet() ?: mutableSetOf()
            current.add(wish)
            prefs[WISHES] = current
        }
    }

    // Smazat přání
    suspend fun removeWish(wish: String) {
        context.dataStore.edit { prefs ->
            val wishes = prefs[WISHES]?.toMutableSet() ?: mutableSetOf()
            val priority = prefs[PRIORITY_WISHES]?.toMutableSet() ?: mutableSetOf()
            val completed = prefs[COMPLETED_WISHES]?.toMutableSet() ?: mutableSetOf()

            wishes.remove(wish)
            priority.remove(wish)
            completed.remove(wish)

            prefs[WISHES] = wishes
            prefs[PRIORITY_WISHES] = priority
            prefs[COMPLETED_WISHES] = completed
        }
    }

    // Přepnout prioritu
    suspend fun togglePriority(wish: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[PRIORITY_WISHES]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(wish)) {
                current.remove(wish)
            } else {
                current.add(wish)
            }
            prefs[PRIORITY_WISHES] = current
        }
    }

    // Přepnout splnění
    suspend fun toggleCompleted(wish: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[COMPLETED_WISHES]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(wish)) {
                current.remove(wish)
            } else {
                current.add(wish)
            }
            prefs[COMPLETED_WISHES] = current
        }
    }
    // Přidat otevřený den
    suspend fun addOpenedDay(day: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[OPENED_DAYS]?.toMutableSet() ?: mutableSetOf()
            current.add(day.toString())
            prefs[OPENED_DAYS] = current
        }
    }

    // Přepnout splnění dne
    suspend fun toggleDayCompleted(day: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[COMPLETED_DAYS]?.toMutableSet() ?: mutableSetOf()
            val dayString = day.toString()
            if (current.contains(dayString)) {
                current.remove(dayString)
            } else {
                current.add(dayString)
            }
            prefs[COMPLETED_DAYS] = current
        }
    }
}