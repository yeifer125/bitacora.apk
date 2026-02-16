package com.tubitacora.plantas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tubitacora.plantas.data.local.dao.*
import com.tubitacora.plantas.data.local.entity.*
import com.tubitacora.plantas.data.local.entity.PlantExpenseEntity
import com.tubitacora.plantas.data.local.dao.PlantExpenseDao

@Database(
    entities = [
        PlantEntity::class,
        LogEntity::class,
        PlantLogEntity::class,      // ✅ ESTA ERA LA QUE FALTABA
        PlantPhotoEntity::class,
        WeatherDecisionEntity::class,
        PlantExpenseEntity::class
    ],
    version = 5,                  // ⬆️ SUBIMOS VERSIÓN
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun plantDao(): PlantDao

    abstract fun plantExpenseDao(): PlantExpenseDao

    abstract fun plantPhotoDao(): PlantPhotoDao
    abstract fun weatherDecisionDao(): WeatherDecisionDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // -------- MIGRATION 2 → 3 --------
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE plants ADD COLUMN lastWatered INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        // -------- MIGRATION 3 → 4 (CLIMA) --------
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS weather_decisions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        plantId INTEGER NOT NULL,
                        date TEXT NOT NULL,
                        shouldWater INTEGER NOT NULL,
                        conditionText TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        // -------- MIGRATION 4 → 5 (PLANT LOGS) ✅ --------
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS plant_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        plantId INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        heightCm REAL,
                        notes TEXT
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "plants_db"
                )
                    .addMigrations(
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5      // ✅ NUEVA MIGRACIÓN
                    )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
