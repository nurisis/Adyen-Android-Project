package com.adyen.android.assignment.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.android.assignment.api.model.VenueResult
import com.adyen.android.assignment.api.usecase.VenueRecommendationsUseCase
import com.adyen.android.assignment.ui.state.MainState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val venueRecommendationsUseCase: VenueRecommendationsUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        @VisibleForTesting
        const val VENUE_RESULT_LIMIT = 50
        private const val KEY_SELECTED_CATEGORY_ID = "KEY_SELECTED_CATEGORY_ID"
    }

    val state = MutableLiveData<MainState>(MainState.Uninitialized)
    private var originalList = emptyList<VenueResult>()

    private var selectedCategoryId: String? = savedStateHandle.get<String>(KEY_SELECTED_CATEGORY_ID)

    fun fetchNearByVenues(latitude: Double?, longitude: Double?) {
        if (latitude == null || longitude == null) {
            state.value = MainState.Error.CurrentLocationFailed
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

                originalList = result?.sortedBy { it.distance } ?: emptyList()

                state.value = if (result?.isNotEmpty() == true) {
                    MainState.PermissionGranted.ShowVenues(
                        list = getVenueListFilteredByCategory(selectedCategoryId, originalList),
                        categoryList = originalList.asSequence()
                            .map { it.categories }
                            .flatten()
                            .distinct()
                            .toList(),
                        selectedCategoryId = selectedCategoryId
                    )
                } else {
                    MainState.PermissionGranted.Empty
                }

            } catch (e: Exception) {
                state.value = MainState.Error.General
            }
        }
    }

    fun selectCategory(categoryId: String?) {
        selectedCategoryId = categoryId
        savedStateHandle.set(KEY_SELECTED_CATEGORY_ID, categoryId)

        val currentState = state.value
        if (currentState is MainState.PermissionGranted.ShowVenues) {
            state.value = currentState.copy(
                list = getVenueListFilteredByCategory(categoryId = categoryId, list = originalList),
                selectedCategoryId = categoryId
            )
        }
    }

    private fun getVenueListFilteredByCategory(
        categoryId: String?,
        list: List<VenueResult>
    ): List<VenueResult> {
        return if (categoryId == null) {
            list
        } else {
            list.filter { result -> result.categories.any { it.id == categoryId } }
        }
    }

    fun setLocationPermissionGranted(isGranted: Boolean) {
        if (isGranted) {
            state.value = MainState.GetCurrentLocation
        } else {
            state.value = MainState.PermissionDenied
        }
    }
}
