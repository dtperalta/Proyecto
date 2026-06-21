package com.example.amls.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilAprendizDao {

    // 1. Guardar un perfil nuevo. Si ya existe uno con el mismo ID, lo reemplaza.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPerfil(perfil: PerfilAprendiz)

    // 2. Leer el perfil del estudiante de forma reactiva (Flow)
    @Query("SELECT * FROM perfil_aprendiz LIMIT 1")
    fun obtenerPerfil(): Flow<PerfilAprendiz?>

    // 3. Actualizar los datos si el estudiante cambia sus preferencias
    @Update
    suspend fun actualizarPerfil(perfil: PerfilAprendiz)
}
