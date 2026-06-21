package com.example.amls.di

import android.content.Context
import com.example.amls.data.AmlsDatabase
import com.example.amls.data.PerfilAprendizDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AmlsDatabase {
        return AmlsDatabase.getDatabase(context)
    }

    @Provides
    fun providePerfilDao(database: AmlsDatabase): PerfilAprendizDao {
        return database.perfilAprendizDao()
    }
}