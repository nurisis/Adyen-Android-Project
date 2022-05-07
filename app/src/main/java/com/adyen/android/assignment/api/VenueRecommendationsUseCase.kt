package com.adyen.android.assignment.api

import com.adyen.android.assignment.api.model.VenueResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class VenueRecommendationsUseCase @Inject constructor() {

    @Throws(IOException::class)
    suspend fun getNearByVenues(
        latitude: Double,
        longitude: Double,
        limit: Int
    ): List<VenueResult>? = withContext(Dispatchers.IO) {
        println("LOG>> [${Thread.currentThread()}] get venues")

        val query = VenueRecommendationsQueryBuilder()
            .setLatitudeLongitude(latitude, longitude)
            .setLimit(limit)
            .build()

        val response = PlacesService.instance
            .getVenueRecommendations(query)
            .execute()

        return@withContext response.body()?.results
    }
}
