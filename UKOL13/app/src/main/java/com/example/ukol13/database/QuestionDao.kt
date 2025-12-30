package com.example.ukol13.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {

    @Insert
    suspend fun insert(question: Question)

    @Insert
    suspend fun insertAll(questions: List<Question>)

    @Query("SELECT * FROM questions WHERE categoryId = :categoryId")
    suspend fun getQuestionsByCategory(categoryId: Int): List<Question>

    @Query("SELECT * FROM questions WHERE categoryId = :categoryId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestions(categoryId: Int, limit: Int): List<Question>

    @Query("SELECT COUNT(*) FROM questions WHERE categoryId = :categoryId")
    suspend fun getQuestionCount(categoryId: Int): Int
}