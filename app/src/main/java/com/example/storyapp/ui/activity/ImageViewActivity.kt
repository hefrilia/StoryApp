package com.example.storyapp.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.storyapp.databinding.ActivityImageViewBinding

class ImageViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityImageViewBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val imageData = intent.getStringExtra(TAG)

        Glide.with(binding.root)
            .load(imageData)
            .into(binding.imageView2)
    }

    companion object{
        const val TAG = "IMAGE_VIEW_TAG"
    }
}