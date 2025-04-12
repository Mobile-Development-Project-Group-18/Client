package com.group18.gosell.data.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    val api: apiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(apiService::class.java)
    }
}
