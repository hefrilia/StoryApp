package com.example.storyapp.ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storyapp.data.database.StoryRepository
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.data.response.StoryResponse
import com.example.storyapp.data.retrofit.ApiConfig
import retrofit2.Response

class StoryViewModel(private val storyRepository: StoryRepository) : ViewModel(){
    private val _story = MutableLiveData<StoryResponse>()
    val story: LiveData<StoryResponse> = _story

    private val _isLoading = MutableLiveData<Boolean>()

    val getPaging: LiveData<PagingData<ListStoryItem>> = storyRepository.getPaging().cachedIn(viewModelScope)

    fun getAllStory(token: String){
        _isLoading.value = true
        val client = ApiConfig.getApiService(token).getStory(location = 1)
        client.enqueue(object : retrofit2.Callback<StoryResponse> {
            override fun onResponse(call: retrofit2.Call<StoryResponse>, response: Response<StoryResponse>) {
                _isLoading.value = false
                if (response.isSuccessful){
                    _story.value = response.body()
                }else {
                    _story.value = response.body()
                    if (response.code() == 401){
                        _story.value = StoryResponse(error = true, message = response.message())
                    }
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }
            override  fun onFailure(call: retrofit2.Call<StoryResponse>, t: Throwable) {
                _isLoading.value = false
                _story.value = StoryResponse(error = true, message = t.message.toString())
                Log.e(TAG, "onFailure Fatal: ${t.message.toString()}")
            }
        })
    }

    companion object{
        private const val TAG = "UserViewModel"
    }

}