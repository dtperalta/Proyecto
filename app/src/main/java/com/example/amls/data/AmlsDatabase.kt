package com.example.amls.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PerfilAprendiz::class], version = 1, exportSchema = false)
abstract class AmlsDatabase : RoomDatabase() {

    abstract fun perfilAprendizDao(): PerfilAprendizDao

    // El patrón para evitar múltiples conexiones
    companion object {
        @Volatile
        private var INSTANCE: AmlsDatabase? = null

        fun getDatabase(context: Context): AmlsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AmlsDatabase::class.java,
                    "amls_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}