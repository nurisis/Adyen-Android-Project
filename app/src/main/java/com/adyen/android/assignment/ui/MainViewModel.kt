package com.adyen.android.assignment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.android.assignment.api.PlacesService
import com.adyen.android.assignment.api.VenueRecommendationsQueryBuilder
import com.adyen.android.assignment.api.model.VenueResult
import com.adyen.android.assignment.ui.state.MainState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    companion object {
        private const val DEFAULT_LATITUDE = 52.376510
        private const val DEFAULT_LONGITUDE = 4.905890
    }

    val state = MutableStateFlow<MainState>(MainState.Uninitialized)

    fun fetchNearByVenues(latitude: Double?, longitude: Double?) {
        state.value = MainState.Loading

        viewModelScope.launch {
            try {
                state.value = getVenues(
                    latitude = latitude ?: DEFAULT_LATITUDE,
                    longitude = longitude ?: DEFAULT_LONGITUDE
                )?.let { list ->
                    MainState.ShowVenues(list = list)
                } ?: MainState.Error

            } catch (e: Exception) {
                state.value = MainState.Error
            }

        }
    }

    @Throws(IOException::class)
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
