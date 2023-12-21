package com.example.storyapp.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.R
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.data.response.StoryResponse
import com.example.storyapp.databinding.ActivityMapsBinding
import com.example.storyapp.helper.AuthData
import com.example.storyapp.helper.DetailData
import com.example.storyapp.ui.viewModel.StoryViewModel
import com.example.storyapp.ui.viewModel.ViewModelFactoryStory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var story: StoryResponse
    private lateinit var storyViewModel: StoryViewModel
    private var token: AuthData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storyViewModel = ViewModelProvider(this, ViewModelFactoryStory(this, token?.token))[StoryViewModel::class.java]

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        token = if (Build.VERSION.SDK_INT >= 33){
            intent.getSerializableExtra(MAPS_ACTIVITY_INTENT_KEY, AuthData::class.java)
        }else{
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(MAPS_ACTIVITY_INTENT_KEY) as AuthData
        }

        storyViewModel.getAllStory(token?.token ?: "")

        storyViewModel.story.observe(this){
            if (it != null){
                Log.d("ERROR", it.toString())
                if (it.error == true){
                    Toast.makeText(this, "Invalid credentials, please login again", Toast.LENGTH_LONG).show()
                }else{
                    story = it
                    pointMarker(story)
                }
            }else{
                Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        val data = storyViewModel.story.value
        if (data != null){
            pointMarker(data)
        }

        val initialLocation = LatLng(-6.8957643, 107.6338462)

        mMap.setOnMarkerClickListener {
            mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(it.position, 15f)))
            it.showInfoWindow()
            true
        }

        mMap.setOnInfoWindowClickListener {
            val markerData = it.tag as ListStoryItem
            val intentDetail = Intent(this, DetailStoryActivity::class.java)
            intentDetail.putExtra(DetailStoryActivity.DETAIL_INTENT_KEY, DetailData(nama = markerData.name!!, image = markerData.photoUrl!!, description = markerData.description!!, time = markerData.createdAt!!))
            startActivity(intentDetail)
            true
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 5f))
        getLocation()
    }

    private fun getLocation(){
        if (ContextCompat.checkSelfPermission(
            this.applicationContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION
            )== PackageManager.PERMISSION_GRANTED
        ){
            mMap.isMyLocationEnabled = true
        }else{
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ){
            isGranted: Boolean ->
            if (isGranted){
                getLocation()
            }
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                mMap.setMapStyle(null)
                true
            }
            R.id.retro_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.retro_style))
                true
            }
            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun pointMarker(story: StoryResponse){
        story.listStory.forEach { data ->
            Log.d("DATANYAPA", data.toString())
            val latLng = LatLng(data.lat?.toDouble() ?: 0.0, data.lon?.toDouble() ?: 0.0)

            mMap.addMarker(
                MarkerOptions()
                .position(latLng)
                .title(data.name)
                .icon(bitmapVector(R.drawable.baseline_location_on_24))
                .snippet(data.description))?.apply {
                tag = data
            }
        }
    }

    private fun bitmapVector(@DrawableRes id: Int): BitmapDescriptor{
        val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
        if (vectorDrawable == null){
            Log.e("Bitmap Helper", "Resource not found")
            return BitmapDescriptorFactory.defaultMarker()
        }
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    companion object{
        const val MAPS_ACTIVITY_INTENT_KEY = "KEY_MAP"
    }
}