package com.example.storyapp.helper

import android.app.Activity
import android.content.Intent
import com.example.storyapp.ui.activity.LoginActivity
import com.example.storyapp.ui.viewModel.AuthViewModel

class AuthHelper {
    companion object{
        fun logOut(context: Activity, tokenViewModel: AuthViewModel){
            tokenViewModel.resetToken()
            val intenDetail = Intent(context, LoginActivity::class.java)
            intenDetail.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity((intenDetail))
        }
    }
}