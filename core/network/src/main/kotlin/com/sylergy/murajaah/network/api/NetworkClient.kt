package com.sylergy.murajaah.network.api

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object AuthTokenHolder {
    @Volatile
    var token: String? = null
}

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        AuthTokenHolder.token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        return chain.proceed(requestBuilder.build())
    }
}

object NetworkClient {
    private const val DEFAULT_BASE_URL = "https://murajaah.sylergy-ltd.workers.dev/"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val sharedOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor())
            .build()
    }

    private val sharedRetrofit: Retrofit by lazy {
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl(DEFAULT_BASE_URL)
            .client(sharedOkHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    private val sharedApiService: MurajaahApiService by lazy {
        sharedRetrofit.create(MurajaahApiService::class.java)
    }

    fun create(
        baseUrl: String = DEFAULT_BASE_URL,
        loggingEnabled: Boolean = false
    ): MurajaahApiService {
        if (baseUrl == DEFAULT_BASE_URL && !loggingEnabled) {
            return sharedApiService
        }

        val clientBuilder = sharedOkHttpClient.newBuilder()
        if (loggingEnabled) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            clientBuilder.addInterceptor(logging)
        }

        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(clientBuilder.build())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(MurajaahApiService::class.java)
    }
}
