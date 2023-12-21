package com.example.storyapp.ui.activity

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.databinding.ActivityAddStoryBinding
import com.example.storyapp.helper.AuthData
import com.example.storyapp.helper.getImageUri
import com.example.storyapp.helper.requestPermissionLauncher
import com.example.storyapp.helper.uriToFile
import com.example.storyapp.ui.components.Button
import com.example.storyapp.ui.viewModel.AddStoryViewModel
import com.example.storyapp.ui.viewModel.StoryViewModel
import com.example.storyapp.ui.viewModel.ViewModelFactoryStory
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import java.util.concurrent.TimeUnit

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var thisButton: Button
    private  lateinit var addStoryViewModel: AddStoryViewModel
    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var currentImageUri: Uri? = null
    private var locationToReq: Location? = null
    private var token: AuthData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermissionLauncher(this, REQUIRED_PERMISSION)

        val actionbar = supportActionBar
        actionbar?.title = "ADD STORY"
        actionbar?.setDisplayHomeAsUpEnabled(true)

        thisButton = binding.btnSubmit

        val storyViewModel = ViewModelProvider(this, ViewModelFactoryStory(this, token?.token))[StoryViewModel::class.java]

        addStoryViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[AddStoryViewModel::class.java]

        val token = if (Build.VERSION.SDK_INT >= 33) {
            intent.getSerializableExtra(ADD_STORY_KEY, AuthData::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(ADD_STORY_KEY) as AuthData
        }

        addStoryViewModel.story.observe(this){ result ->
            setButtonEnable(true)
//            Log.d("APA", result.toString())
            if (result.error == true){
                Toast.makeText(this@AddStoryActivity, result.message, Toast.LENGTH_SHORT).show()
            } else {
//                storyViewModel.getAllStory(token?.token ?: "")
                Toast.makeText(this@AddStoryActivity, result.message, Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }

        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        Log.d("INITOKEN", token.toString())

        addStoryViewModel.isLoading.observe(this){
            setButtonLoading(it)
        }

        binding.btnCamera.setOnClickListener {
            openCamera()
        }

        binding.btnGallery.setOnClickListener {
            openGalerry()
        }

        binding.btnSubmit.setOnClickListener {
            uploadImage(token?.token ?: "")
        }

        binding.checkBox.setOnCheckedChangeListener{_, isChecked ->
            if (isChecked){
                createLocationReq()
            }
        }
    }

    private fun setButtonEnable(value: Boolean) {
        thisButton.isEnabled = value
    }

    private fun setButtonLoading(value: Boolean){
        setButtonEnable(false)
        thisButton.setLoading(value)
    }

    private fun openGalerry() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun uploadImage(token: String) {

        if (currentImageUri != null && binding.editDesc.text.isNotEmpty()){
            setButtonLoading(true)
            currentImageUri?.let { uri ->
                val imageFile = uriToFile(uri, this)
                Log.d("ImageFile", "showImage: ${imageFile.path}")
                val description =  binding.editDesc.text.toString()
                addStoryViewModel.uploadImage(imageFile, description,  lon = locationToReq?.longitude, lat = locationToReq?.latitude, token = token)
            }
        } else {
            Log.d("HASIL", "${currentImageUri}  ${binding.editDesc.text.isNotEmpty()}")
            Toast.makeText(this@AddStoryActivity, "Please select image and fill description", Toast.LENGTH_SHORT).show()
        }

    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("ImageURI", "showImage: $it")
            binding.imgPreview.setImageURI(it)
        }
    }

    private fun openCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getLastLocation()
                }
                else -> {

                }
            }
        }

    private val resolutionLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            RESULT_OK ->
                Log.i(TAG, "onActivityResult: All location settings are satisfied.")
            RESULT_CANCELED ->
                Toast.makeText(
                    this,
                    "You must enable GPS to use this application!",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    private fun createLocationReq(){
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(1)
            maxWaitTime = TimeUnit.SECONDS.toMillis(1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                getLastLocation()
            }
            .addOnFailureListener { exception ->
                binding.checkBox.isChecked = false
                if (exception is ResolvableApiException) {
                    try {
                        resolutionLauncher.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(this, sendEx.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }

    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val mLocationCallback: LocationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    if (locationResult == null) {
                        return
                    }
                    for (location in locationResult.locations) {
                        if (location != null) {

                        }
                    }
                }
            }
            fusedLocation.requestLocationUpdates(locationRequest, mLocationCallback, null)

            fusedLocation.lastLocation.addOnSuccessListener { location: Location? ->

                if (location != null) {
                    locationToReq = location
                    Toast.makeText(
                        this@AddStoryActivity,
                        "${location.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    binding.checkBox.isChecked = false
                    Toast.makeText(
                        this@AddStoryActivity,
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    companion object{
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
        private const val TAG ="MapsActivity"
        const val ADD_STORY_KEY = "ADD_STORY_KEY"
    }

}