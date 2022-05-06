package com.adyen.android.assignment.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adyen.android.assignment.R
import com.adyen.android.assignment.databinding.ActivityMainBinding
import com.adyen.android.assignment.ui.state.MainState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var venuesListAdapter: VenuesListAdapter? = null

    private val locationPermission = Manifest.permission.ACCESS_COARSE_LOCATION
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            handleLocationPermission(isGranted)
        }

    // todo@nurisis: Google play service 설치 여부 체크 https://developers.google.com/android/guides/setup
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initViews()
        observeStates()
    }

    override fun onStart() {
        super.onStart()

        getCurrentLocation()
    }

    private fun observeStates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    handleState(state)
                }
            }
        }
    }

    private fun getCurrentLocation() {
        // check permission
        if (ContextCompat.checkSelfPermission(
                this,
                locationPermission
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.

                    viewModel.fetchNearByVenues(
                        latitude = location?.latitude,
                        longitude = location?.longitude
                    )
                }

        } else {
            requestPermissionLauncher.launch(locationPermission)
        }
    }

    private fun handleLocationPermission(isGranted: Boolean) {
        if (isGranted) {
            getCurrentLocation()
        } else {
            AlertDialog.Builder(this)
                .setMessage(R.string.main_permission_denied_dialog_message)
                .setPositiveButton(R.string.main_permission_denied_dialog_positive_button) { _, _ ->
                    // Navigate to app's setting
                    // todo@nurisis: 여기 코드 개선가능한지 체크 필요
                    startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${packageName}")
                        ).apply {
                            addCategory(Intent.CATEGORY_DEFAULT)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                }
                .setNegativeButton(R.string.main_permission_denied_dialog_negative_button) { dialogInterface, _ ->
                    dialogInterface.cancel()
                }
                .show()
        }
    }

    private fun handleState(state: MainState) {
        when (state) {
            is MainState.Uninitialized -> {
                // todo@nurisis: 상태 처리
            }
            is MainState.Loading -> {
                // todo@nurisis: 상태 처리
            }
            is MainState.Error -> {
                // todo@nurisis: 상태 처리
            }
            is MainState.ShowVenues -> {
                venuesListAdapter?.submitList(state.list)
            }
        }
    }

    private fun initViews() {
        venuesListAdapter = VenuesListAdapter {
            // todo@nurisis: 클릭 처리
            Toast.makeText(this, "Click: ${it.name}", Toast.LENGTH_SHORT).show()
        }

        binding.venuesRecyclerView.adapter = venuesListAdapter

        binding.setCurrentLocationButton.setOnClickListener {
            getCurrentLocation()
        }
    }

}
