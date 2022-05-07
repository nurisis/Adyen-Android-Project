package com.adyen.android.assignment.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adyen.android.assignment.R
import com.adyen.android.assignment.databinding.ActivityMainBinding
import com.adyen.android.assignment.ui.adapter.VenuesListAdapter
import com.adyen.android.assignment.ui.state.MainAction
import com.adyen.android.assignment.ui.state.MainUIState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val venuesListAdapter: VenuesListAdapter by lazy { VenuesListAdapter() }

    private val locationPermission = Manifest.permission.ACCESS_COARSE_LOCATION
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            viewModel.handlePermission(isGranted)
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

        viewModel.getCurrentLocation()
    }

    private fun observeStates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleState(state)
                }
            }
        }

        viewModel.action.observe(this) { action ->
            when (action) {
                MainAction.ShowPermissionDialog -> {
//                    AlertDialog.Builder(this)
//                        .setMessage(R.string.main_permission_denied_dialog_message)
//                        .setPositiveButton(R.string.main_permission_denied_dialog_positive_button) { _, _ ->
//                            // Navigate to app's setting
//                            goToAppSetting()
//                        }
//                        .setNegativeButton(R.string.main_permission_denied_dialog_negative_button) { dialogInterface, _ ->
//                            dialogInterface.cancel()
//                        }
//                        .show()
                }
                MainAction.ClickCurrentLocation -> {
                    getCurrentLocation()
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
                    // todo@nurisis: 여기 리스너 중복으로 불리는건 아닌지 체크 필요.

                    viewModel.fetchNearByVenues(
                        latitude = location?.latitude,
                        longitude = location?.longitude
                    )
                }

        } else {
            requestPermissionLauncher.launch(locationPermission)
        }
    }

    private fun handleState(uiState: MainUIState) {
        binding.loadingView.visibility = View.GONE

        when (uiState) {
            is MainUIState.Uninitialized -> {
                // todo@nurisis: 상태 처리
                binding.coordinator.visibility = View.GONE
                binding.emptyView.visibility = View.GONE
                binding.currentLocationImageView.visibility = View.GONE
            }
            is MainUIState.Loading -> {
                binding.loadingView.visibility = View.VISIBLE
            }
            is MainUIState.PermissionGranted.Empty -> {
                binding.coordinator.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
                binding.currentLocationImageView.visibility = View.GONE

                binding.emptyView.title = getString(R.string.main_permission_granted_empty_title)
                binding.emptyView.message = getString(R.string.main_permission_granted_empty_message)
                binding.emptyView.buttonText = getString(R.string.main_permission_granted_empty_cta)
                binding.emptyView.buttonClickListener = View.OnClickListener {
                    viewModel.getCurrentLocation()
                }
            }
            is MainUIState.PermissionGranted.ShowVenues -> {
                binding.coordinator.visibility = View.VISIBLE
                binding.emptyView.visibility = View.GONE
                binding.currentLocationImageView.visibility = View.VISIBLE

                venuesListAdapter?.submitList(uiState.list)
            }
            is MainUIState.PermissionDenied -> {
                binding.coordinator.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
                binding.currentLocationImageView.visibility = View.GONE

                binding.emptyView.title = getString(R.string.main_permission_denied_empty_title)
                binding.emptyView.message = getString(R.string.main_permission_denied_dialog_message)
                binding.emptyView.buttonText = getString(R.string.main_permission_denied_empty_cta)
                binding.emptyView.buttonClickListener = View.OnClickListener {
                    goToAppSetting()
                }
            }
            is MainUIState.Error -> {
                binding.coordinator.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
                binding.currentLocationImageView.visibility = View.GONE

                binding.emptyView.title = getString(R.string.main_error_title)
                binding.emptyView.message = if (uiState is MainUIState.Error.CurrentLocationFail) {
                    getString(R.string.main_error_get_current_location_message)
                } else getString(R.string.main_error_message)
                binding.emptyView.buttonText = getString(R.string.main_error_cta)
                binding.emptyView.buttonClickListener = View.OnClickListener {
                    viewModel.getCurrentLocation()
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
            viewModel.getCurrentLocation()
        }
    }

}
