package com.adyen.android.assignment.api.usecase

import com.adyen.android.assignment.api.PlacesService
import com.adyen.android.assignment.api.VenueRecommendationsQueryBuilder
import com.adyen.android.assignment.api.model.VenueResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*
import javax.inject.Inject

class VenueRecommendationsUseCase @Inject constructor() {

    @Throws(IOException::class)
    suspend fun getNearByVenues(
        latitude: Double,
        longitude: Double,
        limit: Int
    ): List<VenueResult>? = withContext(Dispatchers.IO) {
        val query = VenueRecommendationsQueryBuilder()
            .setLatitudeLongitude(latitude, longitude)
            .setLimit(limit)
            .build()

        val response = PlacesService.instance
            .getVenueRecommendations(query = query, language = Locale.getDefault().language)
            .execute()

        return@withContext response.body()?.results
    }
}
