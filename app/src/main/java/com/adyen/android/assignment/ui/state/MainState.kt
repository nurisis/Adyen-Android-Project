package com.adyen.android.assignment.ui.state

import com.adyen.android.assignment.api.model.VenueResult

sealed class MainState {
    object Uninitialized : MainState()

    data class ShowVenues(
        val list: List<VenueResult>
    ): MainState()

    object Loading : MainState()
    object Error : MainState()
}
