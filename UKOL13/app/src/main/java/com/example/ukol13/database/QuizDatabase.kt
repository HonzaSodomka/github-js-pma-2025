package com.example.ukol13.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Category::class, Question::class, GameResult::class],
    version = 1,
    exportSchema = false
)
abstract class QuizDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun questionDao(): QuestionDao
    abstract fun gameResultDao(): GameResultDao

    companion object {
        @Volatile
        private var INSTANCE: QuizDatabase? = null

        fun getDatabase(context: Context): QuizDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    "quiz_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database)
                    }
                }
            }
        }

        suspend fun populateDatabase(database: QuizDatabase) {
            val categoryDao = database.categoryDao()
            val questionDao = database.questionDao()

            // Přidej kategorie
            categoryDao.insert(Category(id = 1, name = "Geografie", icon = "🌍"))
            categoryDao.insert(Category(id = 2, name = "Historie", icon = "📜"))
            categoryDao.insert(Category(id = 3, name = "Sport", icon = "⚽"))

            // Přidej otázky - Geografie
            val geoQuestions = listOf(
                Question(0, 1, "Jaké je hlavní město Francie?", "Berlín", "Madrid", "Paříž", "Řím", 2),
                Question(0, 1, "Který oceán je největší?", "Atlantský", "Tichý", "Indický", "Severní ledový", 1),
                Question(0, 1, "Ve které zemi leží Praha?", "Slovensko", "Polsko", "Česko", "Rakousko", 2),
                Question(0, 1, "Jaká je nejvyšší hora světa?", "K2", "Kilimandžáro", "Mont Blanc", "Mount Everest", 3),
                Question(0, 1, "Která řeka protéká Londýnem?", "Temže", "Seina", "Rýn", "Dunaj", 0)
            )

            // Přidej otázky - Historie
            val historyQuestions = listOf(
                Question(0, 2, "V kterém roce skončila 2. světová válka?", "1943", "1944", "1945", "1946", 2),
                Question(0, 2, "Kdo byl prvním prezidentem USA?", "Thomas Jefferson", "George Washington", "Abraham Lincoln", "John Adams", 1),
                Question(0, 2, "Kdy byl podepsán Manifest CN?", "1848", "1789", "1917", "1945", 0),
                Question(0, 2, "Kdo vynalezl žárovku?", "Nikola Tesla", "Thomas Edison", "Benjamin Franklin", "Alexander Bell", 1),
                Question(0, 2, "Kdy byl založen stát Československo?", "1916", "1918", "1920", "1945", 1)
            )

            // Přidej otázky - Sport
            val sportQuestions = listOf(
                Question(0, 3, "Kolik hráčů má fotbalový tým na hřišti?", "9", "10", "11", "12", 2),
                Question(0, 3, "V jakém sportu vyhrál Usain Bolt?", "Plavání", "Atletika", "Box", "Cyklistika", 1),
                Question(0, 3, "Kolik set má tenisový zápas?", "2", "3", "4", "5", 1),
                Question(0, 3, "Kdy se konaly olympijské hry v Praze?", "Nikdy", "1980", "1984", "2000", 0),
                Question(0, 3, "Jaký sport hraje Cristiano Ronaldo?", "Basketbal", "Hokej", "Fotbal", "Tenis", 2)
            )

            questionDao.insertAll(geoQuestions)
            questionDao.insertAll(historyQuestions)
            questionDao.insertAll(sportQuestions)
        }
    }
}