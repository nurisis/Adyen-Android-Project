package com.adyen.android.assignment.ui.state

import com.adyen.android.assignment.api.model.Category
import com.adyen.android.assignment.api.model.VenueResult

sealed class MainState {
    object Uninitialized : MainState()

    object Loading : MainState()

    object GetCurrentLocation : MainState()

    object PermissionDenied : MainState()

    sealed class PermissionGranted : MainState() {

        object Empty : PermissionGranted()

        data class ShowVenues(
            val list: List<VenueResult>,
            val categoryList: List<Category>,
            val selectedCategoryId: String?
        ) : MainState()
    }

    sealed class Error : MainState() {
        object General : Error()

        object CurrentLocationFailed : Error()
    }
}
