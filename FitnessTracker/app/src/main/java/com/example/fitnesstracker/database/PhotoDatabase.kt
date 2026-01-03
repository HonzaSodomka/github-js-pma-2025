package com.example.fitnesstracker.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. Tabulka (Entity)
@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val filePath: String,      // Cesta k souboru v telefonu
    val dateTimestamp: Long    // Datum vytvoření
)

// 2. Příkazy (DAO)
@Dao
interface PhotoDao {
    // Načte všechny fotky, seřazené od nejnovější
    @Query("SELECT * FROM photos ORDER BY dateTimestamp DESC")
    suspend fun getAllPhotos(): List<PhotoEntity>

    // Vloží novou fotku
    @Insert
    suspend fun insertPhoto(photo: PhotoEntity)

    // Smaže fotku podle ID (pro PhotoDetailActivity)
    @Query("DELETE FROM photos WHERE id = :photoId")
    suspend fun deleteById(photoId: Int)
}

// 3. Databáze (Database)
@Database(entities = [PhotoEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_tracker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
