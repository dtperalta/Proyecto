package com.example.amls.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecursoEducativoDao {
    // Inserta una lista de recursos. Si ya existen, los reemplaza para actualizarlos.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRecursos(recursos: List<RecursoEducativo>)

    // Lee los recursos para mostrarlos en la pantalla sin importar si hay internet
    @Query("SELECT * FROM recursos_educativos")
    fun obtenerRecursosLocales(): Flow<List<RecursoEducativo>>
}
