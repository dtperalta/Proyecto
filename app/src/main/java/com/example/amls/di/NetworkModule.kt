package com.example.amls.di

import com.example.amls.auth.TokenManager
import com.example.amls.network.AmlsApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://amls-backend.onrender.com/"

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val rutasPublicas = listOf("/auth/register", "/auth/login")

            if (rutasPublicas.any { original.url.encodedPath.endsWith(it) }) {
                chain.proceed(original)
            } else {
                val token = tokenManager.obtenerToken()
                val request = if (token != null) {
                    original.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    original
                }
                chain.proceed(request)
            }
        }
    }

    @Provides
    @Singleton
    fun provideRetrofit(authInterceptor: Interceptor): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
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
