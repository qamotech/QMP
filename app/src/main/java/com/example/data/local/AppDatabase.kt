package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BeatProject::class, SavedAudioStem::class, StudioPreset::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun beatProjectDao(): BeatProjectDao
    abstract fun studioPresetDao(): StudioPresetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "beatbox_studio_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
