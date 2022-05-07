package com.adyen.android.assignment.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.android.assignment.api.PlacesService
import com.adyen.android.assignment.api.VenueRecommendationsQueryBuilder
import com.adyen.android.assignment.api.model.VenueResult
import com.adyen.android.assignment.ui.state.MainAction
import com.adyen.android.assignment.ui.state.MainUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    companion object {
        private const val DEFAULT_LATITUDE = 52.376510
        private const val DEFAULT_LONGITUDE = 4.905890
    }

    val state = MutableStateFlow<MainUIState>(MainUIState.Uninitialized)
    val action = MutableLiveData<MainAction>()

    fun fetchNearByVenues(latitude: Double?, longitude: Double?) {
        if (latitude == null || longitude == null) {
            state.value = MainUIState.Error.CurrentLocationFail
            return
        }

        state.value = MainUIState.Loading

        viewModelScope.launch {
            try {
                state.value = getVenues(
                    latitude = latitude,
                    longitude = longitude
                )?.let { list ->
                    if (list.isEmpty()) {
                        MainUIState.PermissionGranted.Empty
                    } else MainUIState.PermissionGranted.ShowVenues(list = list)
                } ?: MainUIState.PermissionGranted.Empty

            } catch (e: Exception) {
                state.value = MainUIState.Error.General
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
            state.value = MainUIState.PermissionDenied
            action.value = MainAction.ShowPermissionDialog
        }
    }

    private suspend fun getVenues(latitude: Double, longitude: Double): List<VenueResult>? =
        withContext(Dispatchers.IO) {
            val query = VenueRecommendationsQueryBuilder()
                .setLatitudeLongitude(latitude, longitude)
                .build()
            val response = PlacesService.instance
                .getVenueRecommendations(query)
                .execute()

            val responseWrapper = response.body()

            return@withContext responseWrapper?.results
        }
}
