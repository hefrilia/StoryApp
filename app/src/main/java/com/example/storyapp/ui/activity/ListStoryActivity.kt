package com.example.storyapp.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storyapp.R
import com.example.storyapp.data.preferences.AuthPreferences
import com.example.storyapp.data.preferences.dataStore
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.databinding.ActivityListStoryBinding
import com.example.storyapp.helper.AuthData
import com.example.storyapp.helper.AuthHelper
import com.example.storyapp.ui.adapter.StoryAdapter
import com.example.storyapp.ui.viewModel.AuthViewModel
import com.example.storyapp.ui.viewModel.StoryViewModel
import com.example.storyapp.ui.viewModel.ViewModelFactory
import com.example.storyapp.ui.viewModel.ViewModelFactoryStory

class ListStoryActivity : AppCompatActivity() {

    private var token: AuthData? = null
    private lateinit var binding: ActivityListStoryBinding
    private lateinit var adapter: StoryAdapter
    private lateinit var storyViewModel : StoryViewModel
    private lateinit var tokenViewModel: AuthViewModel

    companion object{
        const val TOKEN_INTENT_KEY = "TOKEN_KEY"
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK){
                adapter.refresh()
                adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){
                    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                        if (positionStart == 0){
                            binding.rvStory.scrollToPosition(0)
                        }
                    }
                })
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = StoryAdapter()
        binding.rvStory.adapter = adapter

        val pref = AuthPreferences.getInstance(application.dataStore)
        tokenViewModel = ViewModelProvider(this, ViewModelFactory(pref))[AuthViewModel::class.java]

        token = if (Build.VERSION.SDK_INT >= 33){
            intent.getSerializableExtra(TOKEN_INTENT_KEY, AuthData::class.java)
        }else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra((TOKEN_INTENT_KEY)) as AuthData
        }

        storyViewModel = ViewModelProvider(this, ViewModelFactoryStory(this, token?.token))[StoryViewModel::class.java]

        getData()

        val layoutManager = LinearLayoutManager(this)
        binding.rvStory.layoutManager = layoutManager

        binding.btnAddStory.setOnClickListener{
            val intentDetail = Intent(this, AddStoryActivity::class.java)
            intentDetail.putExtra(AddStoryActivity.ADD_STORY_KEY, AuthData(nama = token?.nama, userId = token?.userId, token = token?.token))
            getResult.launch(intentDetail)
        }

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = true
            getData()
            Toast.makeText(this, "List stories refreshed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_navigation, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.btn_logout -> {
                AuthHelper.logOut(this, tokenViewModel = tokenViewModel )
                true
            }
            R.id.btn_maps -> {
                val intentDetail = Intent(this, MapsActivity::class.java)
                intentDetail.putExtra(MapsActivity.MAPS_ACTIVITY_INTENT_KEY, AuthData(nama = token?.nama, userId = token?.userId, token = token?.token))
                startActivity(intentDetail)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun setStory(story: PagingData<ListStoryItem>){
        adapter.submitData(lifecycle, story)
        adapter.setOnItemClickCallback(object : StoryAdapter.OnItemClickCallback{
            override fun onItemClicked(data: ListStoryItem) {
                
            }
        })
    }

    private fun getData(){
        storyViewModel.getPaging.observe(this){
            binding.swipeRefresh.isRefreshing = false
            if (it != null){
                Log.d("ISERROR", it.toString())
                setStory(it)

            } else {

            }
        }
    }

}