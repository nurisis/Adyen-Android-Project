package com.adyen.android.assignment.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.android.assignment.api.VenueRecommendationsUseCase
import com.adyen.android.assignment.ui.state.MainState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val venueRecommendationsUseCase: VenueRecommendationsUseCase,
) : ViewModel() {

    companion object {
        @VisibleForTesting const val VENUE_RESULT_LIMIT = 50
    }

    val state = MutableLiveData<MainState>(MainState.Uninitialized)

    // Remember scroll position of the list for configuration changes
    private var scrollPosition : Int? = null

    fun fetchNearByVenues(latitude: Double?, longitude: Double?) {
        if (latitude == null || longitude == null) {
            state.value = MainState.Error.CurrentLocationFail
            return
        }

        state.value = MainState.Loading

        viewModelScope.launch {
            try {
                val result = venueRecommendationsUseCase.getNearByVenues(
                    latitude = latitude,
                    longitude = longitude,
                    limit = VENUE_RESULT_LIMIT
                )

                state.value = if (result?.isNotEmpty() == true) {
                    MainState.PermissionGranted.ShowVenues(
                        list = result.sortedBy { it.distance },
                        scrollPosition = scrollPosition
                    )
                } else {
                    MainState.PermissionGranted.Empty
                }

                setScrollPosition(null)

            } catch (e: Exception) {
                state.value = MainState.Error.General
            }
        }
    }

    fun setLocationPermissionGranted(isGranted: Boolean) {
        if (isGranted) {
            state.value = MainState.GetCurrentLocation
        } else {
            state.value = MainState.PermissionDenied
        }
    }

    fun setScrollPosition(position: Int?) {
        scrollPosition = position
    }

}
