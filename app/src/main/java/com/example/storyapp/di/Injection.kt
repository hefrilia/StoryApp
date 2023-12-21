package com.example.storyapp.di

import android.content.Context
import com.example.storyapp.data.database.StoryDatabase
import com.example.storyapp.data.database.StoryRepository
import com.example.storyapp.data.retrofit.ApiConfig

object Injection {
    fun provideRepository(context: Context, token: String? = null): StoryRepository{
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService(token)
        return StoryRepository(database, apiService)
    }
}