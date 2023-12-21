package com.example.storyapp.ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storyapp.data.response.AuthResponse
import com.example.storyapp.data.retrofit.ApiConfig
import retrofit2.Response

class RegisterViewModel : ViewModel() {

    private val _auth = MutableLiveData<AuthResponse>()
    val auth: LiveData<AuthResponse> = _auth

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun register(formRegister: FormRegister){
        _isLoading.value = true
        val client = ApiConfig.getApiService().register( email = formRegister.email, password = formRegister.password, name = formRegister.name)
        client.enqueue (object : retrofit2.Callback<AuthResponse> {
            override fun onResponse(
                call: retrofit2.Call<AuthResponse>,
                response: Response<AuthResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _auth.value = response.body()
                } else {
                    _auth.value = AuthResponse(error = true, message = response.message())
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<AuthResponse>, t: Throwable) {
                _isLoading.value = false
                if (t.message.toString().startsWith("Failed to Connect")) {
                    _auth.value = AuthResponse(error = true, message = "Failed to Connect API")
                }
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    companion object{
        private const val TAG = "REGISTER_VIEW_MODEL"
        data class FormRegister(val name: String, val email: String, val password: String)
    }
}