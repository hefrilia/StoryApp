package com.example.storyapp.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.storyapp.databinding.ActivityDetailStoryBinding
import com.example.storyapp.helper.DetailData

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionbar = supportActionBar
        actionbar?.title = "Detail Story"
        actionbar?.setDisplayHomeAsUpEnabled(true)

        val detailData = if (Build.VERSION.SDK_INT >= 33){
            intent.getSerializableExtra(DETAIL_INTENT_KEY, DetailData::class.java)
        }else{
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(DETAIL_INTENT_KEY) as DetailData
        }

        binding.nameUser.text = detailData?.nama
        binding.description.text = detailData?.description

        binding.imgUser.setOnClickListener{
            val intentDetail = Intent(this, ImageViewActivity::class.java)
            intentDetail.putExtra(ImageViewActivity.TAG, detailData?.image)
            startActivity(intentDetail)
        }

        var requestOptions = RequestOptions()
        requestOptions = requestOptions.transform(CenterCrop(), RoundedCorners(15))

        Glide.with(binding.root)
            .load(detailData?.image)
            .apply(requestOptions)
            .into((binding.imgUser))
    }

    companion object{
        const val DETAIL_INTENT_KEY ="DETAIL FOR STORY"
    }
}