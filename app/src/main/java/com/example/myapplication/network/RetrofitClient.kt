package com.example.myapplication.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://testbk-zeta.vercel.app/api/"

    // 配置 OkHttpClient 增加超时时间
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)    // 连接超时
        .writeTimeout(30, TimeUnit.SECONDS)      // 写入超时
        .readTimeout(30, TimeUnit.SECONDS)       // 读取超时
        .build()

    // 创建 Retrofit 实例并应用 OkHttpClient
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)  // 使用自定义的 OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}