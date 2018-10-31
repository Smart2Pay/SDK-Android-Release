package com.smart2pay.example.requests

import android.annotation.SuppressLint
import android.content.Context
import com.smart2pay.example.misc.Constants
import com.smart2pay.example.requests.converters.JsonConverterFactory
import com.smart2pay.example.requests.converters.StringConverterFactory
import com.smart2pay.example.requests.interceptors.LoggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class RequestManager private constructor(private val context: Context) {
    private val retrofit: Retrofit
    val api: Api
    private val okHttpClient: OkHttpClient
    var unauthorizedHandler: UnauthorizedHandler? = null

    init {
        val okHttpBuilder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if(Constants.isDebug) {
            okHttpBuilder.addNetworkInterceptor(LoggingInterceptor())
        }
        okHttpBuilder.addInterceptor { chain ->
            val originalRequest = chain.request()
            val newRequestBuilder = originalRequest.newBuilder()
            newRequestBuilder.header("Authorization", Constants.apiToken)
            return@addInterceptor chain.proceed(newRequestBuilder.build())
        }
        okHttpClient = okHttpBuilder.build()

        retrofit = Retrofit.Builder()
            .baseUrl(Constants.apiUrl)
            .addConverterFactory(StringConverterFactory())
            .addConverterFactory(JsonConverterFactory())
            .client(okHttpClient)
            .build()

        api = retrofit.create(Api::class.java)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: RequestManager
            @Synchronized private set
            @Synchronized get

        @Synchronized
        fun initialize(context: Context) {
            instance = RequestManager(context.applicationContext)
        }
    }
}