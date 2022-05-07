package com.adyen.android.assignment.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.adyen.android.assignment.R
import com.adyen.android.assignment.databinding.ActivityMainBinding
import com.adyen.android.assignment.ui.adapter.VenuesListAdapter
import com.adyen.android.assignment.ui.state.MainState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val venuesListAdapter: VenuesListAdapter by lazy { VenuesListAdapter() }

    private val requestFineLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            viewModel.setLocationPermissionGranted(isGranted = isGranted || isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION))
        }

    private val requestLocationPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            viewModel.setLocationPermissionGranted(
                isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            )
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

        checkLocationPermissions()
    }

    private fun observeStates() {
        viewModel.state.observe(this) { state ->
            handleState(state)
        }
    }

    private fun checkLocationPermissions() {
        when {
            /**
             * Case 1. Grant Precise Location
             */
            isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                isPermissionGranted(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) -> {
                viewModel.setLocationPermissionGranted(isGranted = true)
            }
            /**
             * Case 2. Grant Approximate Location
             * */
            isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION) -> {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                ) {
                    AlertDialog.Builder(this)
                        .setMessage(R.string.main_fine_location_permission_dialog_message)
                        .setPositiveButton(R.string.main_fine_location_permission_dialog_positive_button) { _, _ ->
                            // request permission again
                            requestFineLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    viewModel.setLocationPermissionGranted(isGranted = true)
                }
            }
            /**
             * Case 3. All denied
             * */
            else -> {
                requestLocationPermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }
    }

    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->

                viewModel.fetchNearByVenues(
                    latitude = location?.latitude,
                    longitude = location?.longitude
                )
            }
        } catch (e: SecurityException) {
            checkLocationPermissions()
        }
    }

    private fun isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun handleState(state: MainState) {
        binding.loadingView.visibility = View.GONE

        when (state) {
            is MainState.Uninitialized -> {
                binding.coordinator.visibility = View.GONE
                binding.emptyView.visibility = View.GONE
                binding.currentLocationImageView.visibility = View.GONE
            }
            is MainState.Loading -> {
                binding.loadingView.visibility = View.VISIBLE
            }
            is MainState.PermissionGranted.Empty -> {
                binding.coordinator.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
                binding.currentLocationImageView.visibility = View.GONE

                binding.emptyView.title = getString(R.string.main_permission_granted_empty_title)
                binding.emptyView.message =
                    getString(R.string.main_permission_granted_empty_message)
                binding.emptyView.buttonText = getString(R.string.main_permission_granted_empty_cta)
                binding.emptyView.buttonClickListener = View.OnClickListener {
                    checkLocationPermissions()
                }
            }
            is MainState.PermissionGranted.ShowVenues -> {
                binding.coordinator.visibility = View.VISIBLE
                binding.emptyView.visibility = View.GONE
                binding.currentLocationImageView.visibility = View.VISIBLE

                venuesListAdapter.submitList(state.list)
            }
            is MainState.PermissionDenied -> {
                binding.coordinator.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
                binding.currentLocationImageView.visibility = View.GONE

                binding.emptyView.title = getString(R.string.main_permission_denied_empty_title)
                binding.emptyView.message =
                    getString(R.string.main_fine_location_permission_dialog_message)
                binding.emptyView.buttonText = getString(R.string.main_permission_denied_empty_cta)
                binding.emptyView.buttonClickListener = View.OnClickListener {
                    goToAppSetting()
                }
            }
            is MainState.GetCurrentLocation -> {
                getCurrentLocation()
            }
            is MainState.Error -> {
                binding.coordinator.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
                binding.currentLocationImageView.visibility = View.GONE

                binding.emptyView.title = getString(R.string.main_error_title)
                binding.emptyView.message = if (state is MainState.Error.CurrentLocationFail) {
                    getString(R.string.main_error_get_current_location_message)
                } else getString(R.string.main_error_message)
                binding.emptyView.buttonText = getString(R.string.main_error_cta)
                binding.emptyView.buttonClickListener = View.OnClickListener {
                    checkLocationPermissions()
                }
            }
        }
    }

    private fun goToAppSetting() {
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

    private fun initViews() {
        binding.venuesRecyclerView.adapter = venuesListAdapter

        binding.currentLocationImageView.setOnClickListener {
            checkLocationPermissions()
        }
    }

}
