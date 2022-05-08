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
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adyen.android.assignment.R
import com.adyen.android.assignment.databinding.ActivityMainBinding
import com.adyen.android.assignment.ui.adapter.VenueListAdapter
import com.adyen.android.assignment.ui.state.MainState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val venueListAdapter: VenueListAdapter by lazy { VenueListAdapter() }

    private val scrollPositionKey = "KEY_SCROLL_POSITION"

    // Remember scroll position of the list for configuration changes
    private var scrollPosition: Int? = null

    private val requestFineLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            viewModel.setLocationPermissionGranted(
                isGranted = isGranted || isPermissionGranted(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

    private val requestLocationPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            viewModel.setLocationPermissionGranted(
                isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            )
        }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        observeStates()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        scrollPosition = savedInstanceState?.getInt(scrollPositionKey)
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
                isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) -> {
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
                binding.venuesRecyclerView.visibility = View.GONE
                binding.emptyView.visibility = View.GONE
                binding.currentLocationImageView.visibility = View.GONE
            }
            is MainState.Loading -> {
                binding.loadingView.visibility = View.VISIBLE
            }
            is MainState.GetCurrentLocation -> {
                getCurrentLocation()
            }
            is MainState.PermissionGranted.Empty -> {
                showEmptyView(
                    titleResId = R.string.main_permission_granted_empty_title,
                    messageResId = R.string.main_permission_granted_empty_message,
                    imageResId = R.drawable.ic_refresh_48,
                    buttonTextResId = R.string.retry,
                    buttonClickListener = {
                        getCurrentLocation()
                    }
                )
            }
            is MainState.PermissionGranted.ShowVenues -> {
                binding.venuesRecyclerView.visibility = View.VISIBLE
                binding.currentLocationImageView.visibility = View.VISIBLE
                binding.emptyView.visibility = View.GONE

                venueListAdapter.submitList(state.list) {
                    scrollPosition?.let {
                        binding.venuesRecyclerView.scrollToPosition(it)

                        scrollPosition = null
                    }
                }
            }
            is MainState.PermissionDenied -> {
                showEmptyView(
                    titleResId = R.string.main_permission_denied_empty_title,
                    messageResId = R.string.main_fine_location_permission_dialog_message,
                    imageResId = R.drawable.ic_my_location_48,
                    buttonTextResId = R.string.main_permission_denied_empty_cta,
                    buttonClickListener = {
                        goToAppSetting()
                    }
                )
            }
            is MainState.Error -> {
                showEmptyView(
                    titleResId = R.string.main_error_title,
                    messageResId = if (state is MainState.Error.CurrentLocationFailed) {
                        R.string.main_error_get_current_location_message
                    } else R.string.main_error_message,
                    imageResId = R.drawable.ic_refresh_48,
                    buttonTextResId = R.string.retry,
                    buttonClickListener = {
                        checkLocationPermissions()
                    }
                )
            }
        }
    }

    private fun goToAppSetting() {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${packageName}")
            ).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }

    private fun initViews() {
        binding.venuesRecyclerView.adapter = venueListAdapter
        binding.venuesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                setScrollTopIconVisibility()
            }
        })

        binding.currentLocationImageView.setOnClickListener {
            checkLocationPermissions()
        }

        setScrollTopIconVisibility()
        binding.scrollTopImageView.setOnClickListener {
            binding.venuesRecyclerView.smoothScrollToPosition(0)
        }
    }

    private fun setScrollTopIconVisibility() {
        if (binding.venuesRecyclerView.canScrollVertically(-1)) {
            binding.scrollTopImageView.visibility = View.VISIBLE
        } else {
            binding.scrollTopImageView.visibility = View.GONE
        }
    }

    private fun showEmptyView(
        @StringRes titleResId: Int,
        @StringRes messageResId: Int,
        @DrawableRes imageResId: Int,
        @StringRes buttonTextResId: Int,
        buttonClickListener: View.OnClickListener,
    ) {
        binding.emptyView.visibility = View.VISIBLE
        binding.venuesRecyclerView.visibility = View.GONE
        binding.currentLocationImageView.visibility = View.GONE

        binding.emptyView.title = getString(titleResId)
        binding.emptyView.message = getString(messageResId)
        binding.emptyView.buttonText = getString(buttonTextResId)
        binding.emptyView.imageResId = imageResId
        binding.emptyView.buttonClickListener = buttonClickListener
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        (binding.venuesRecyclerView.layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition()
            ?.let {
                outState.putInt(scrollPositionKey, it)
            }
    }

}
