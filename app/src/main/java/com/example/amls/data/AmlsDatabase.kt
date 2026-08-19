package com.example.amls.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Aumentamos la versión a 2 y agregamos la nueva entidad
@Database(entities = [PerfilAprendiz::class, RecursoEducativo::class], version = 6, exportSchema = false)
abstract class AmlsDatabase : RoomDatabase() {
    abstract fun perfilAprendizDao(): PerfilAprendizDao
    abstract fun recursoEducativoDao(): RecursoEducativoDao // Nuevo DAO

    companion object {
        @Volatile
        private var INSTANCE: AmlsDatabase? = null

        fun getDatabase(context: Context): AmlsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AmlsDatabase::class.java,
                    "amls_database"
                )
                .fallbackToDestructiveMigration() // Destruye y recrea la DB al cambiar de versión
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
