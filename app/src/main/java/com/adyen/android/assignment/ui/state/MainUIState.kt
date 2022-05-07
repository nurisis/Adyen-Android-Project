package com.adyen.android.assignment.ui.state

import com.adyen.android.assignment.api.model.VenueResult

sealed class MainUIState {
    object Uninitialized : MainUIState()

    object Loading : MainUIState()

    object PermissionDenied : MainUIState()

    sealed class PermissionGranted : MainUIState() {

        object Empty : PermissionGranted()

        data class ShowVenues(
            val list: List<VenueResult>
        ) : MainUIState()
    }

    sealed class Error : MainUIState() {
        object General : Error()

        object CurrentLocationFail : Error()
    }
}
