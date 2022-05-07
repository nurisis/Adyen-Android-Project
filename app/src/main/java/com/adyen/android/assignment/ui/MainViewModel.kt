package com.adyen.android.assignment.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.android.assignment.api.VenueRecommendationsUseCase
import com.adyen.android.assignment.ui.state.MainAction
import com.adyen.android.assignment.ui.state.MainUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val venueRecommendationsUseCase: VenueRecommendationsUseCase
) : ViewModel() {

    companion object {
        private const val DEFAULT_LATITUDE = 52.376510
        private const val DEFAULT_LONGITUDE = 4.905890
        private const val VENUE_RESULT_LIMIT = 50
    }

    val uiState = MutableStateFlow<MainUIState>(MainUIState.Uninitialized)
    val action = MutableLiveData<MainAction>()

    fun fetchNearByVenues(latitude: Double?, longitude: Double?) {
        if (latitude == null || longitude == null) {
            uiState.value = MainUIState.Error.CurrentLocationFail
            return
        }

        uiState.value = MainUIState.Loading

        viewModelScope.launch {
            try {
                val result = venueRecommendationsUseCase.getNearByVenues(
                    latitude = latitude,
                    longitude = longitude,
                    limit = VENUE_RESULT_LIMIT
                )

                uiState.value = if (result?.isNotEmpty() == true) {
                    MainUIState.PermissionGranted.ShowVenues(list = result.sortedBy { it.distance })
                } else {
                    MainUIState.PermissionGranted.Empty
                }

            } catch (e: Exception) {
                uiState.value = MainUIState.Error.General
            }

        }
    }

    // todo@nurisis: 음 여기 개선 필요
    fun getCurrentLocation() {
        action.value = MainAction.ClickCurrentLocation
    }

    fun handlePermission(isGranted: Boolean) {
        if (isGranted) {
            action.value = MainAction.ClickCurrentLocation
        } else {
            uiState.value = MainUIState.PermissionDenied
            action.value = MainAction.ShowPermissionDialog
        }
    }
}
