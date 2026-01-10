package com.example.ukolnicek.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.ukolnicek.models.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // Pro nesplněné úkoly (podle deadlinu)
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY deadlineTimestamp ASC")
    fun getActiveTasks(): Flow<List<Task>>

    // Pro splněné úkoly (podle toho kdy byly splněny - zjednodušeně ID)
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY id DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
    
    // Pro obnovení smazaného (Undo)
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?
}

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ukolnicek_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
