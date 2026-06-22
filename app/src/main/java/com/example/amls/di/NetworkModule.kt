package com.example.amls.di

import com.example.amls.network.AmlsApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        // Interceptor para ver las peticiones en la consola (Logcat)
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        // IMPORTANTE: Cambia "10.0.2.2" por la IP de tu computadora (ej. 192.168.1.XX)
        // si usas un dispositivo físico. "10.0.2.2" es el equivalente a "localhost" para el Emulador de Android.
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAmlsApiService(retrofit: Retrofit): AmlsApiService {
        return retrofit.create(AmlsApiService::class.java)
    }
}