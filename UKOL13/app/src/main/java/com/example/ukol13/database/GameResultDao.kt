package com.example.ukol13.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameResultDao {

    @Insert
    suspend fun insert(result: GameResult)

    @Query("SELECT * FROM game_results WHERE userId = :userId ORDER BY timestamp DESC")
    fun getResultsByUser(userId: Int): Flow<List<GameResult>>

    @Query("SELECT * FROM game_results WHERE userId = :userId AND categoryId = :categoryId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastResultByUserAndCategory(userId: Int, categoryId: Int): GameResult?

    @Query("SELECT * FROM game_results ORDER BY timestamp DESC LIMIT 20")
    fun getRecentResults(): Flow<List<GameResult>>

    @Query("SELECT AVG(score) FROM game_results WHERE userId = :userId")
    suspend fun getAverageScore(userId: Int): Double?
}