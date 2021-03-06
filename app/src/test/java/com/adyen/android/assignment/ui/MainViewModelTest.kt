package com.adyen.android.assignment.ui

import androidx.lifecycle.SavedStateHandle
import com.adyen.android.assignment.api.model.*
import com.adyen.android.assignment.api.usecase.VenueRecommendationsUseCase
import com.adyen.android.assignment.base.BaseViewModelTest
import com.adyen.android.assignment.ui.state.MainState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import java.io.IOException

class MainViewModelTest : BaseViewModelTest() {

    private lateinit var viewModel: MainViewModel

    @Mock
    lateinit var venueRecommendationsUseCase: VenueRecommendationsUseCase

    @Mock
    lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun initViewModel() {
        viewModel = MainViewModel(
            venueRecommendationsUseCase,
            savedStateHandle
        )
    }

    @Test
    fun `After receiving permission approval & current location, Shows a list of nearby places`() =
        runTest {
            val testObserver = viewModel.state.test()

            // given
            val mockResult = mockResult()
            val mockLatitude = 1.1
            val mockLongitude = 1.1
            Mockito.`when`(
                venueRecommendationsUseCase.getNearByVenues(
                    mockLatitude,
                    mockLongitude,
                    MainViewModel.VENUE_RESULT_LIMIT
                )
            ).thenReturn(mockResult)

            // when
            viewModel.setLocationPermissionGranted(isGranted = true)
            viewModel.fetchNearByVenues(mockLatitude, mockLongitude)

            // then
            testObserver.assertValueSequence(
                listOf(
                    MainState.Uninitialized,
                    MainState.GetCurrentLocation,
                    MainState.Loading,
                    MainState.PermissionGranted.ShowVenues(
                        list = mockResult,
                        selectedCategoryId = null,
                        categoryList = listOf(
                            Category(Icon("", ""), name = "cate1", id = "123"),
                            Category(Icon("", ""), name = "cate2", id = "124"),
                            Category(Icon("", ""), name = "cate5", id = "125")
                        )
                    )
                )
            )
        }

    @Test
    fun `When there are no places around the current location, Shows empty view`() = runTest {
        val testObserver = viewModel.state.test()

        // given
        val mockLatitude = 1.1
        val mockLongitude = 1.1
        Mockito.`when`(
            venueRecommendationsUseCase.getNearByVenues(
                mockLatitude,
                mockLongitude,
                MainViewModel.VENUE_RESULT_LIMIT
            )
        ).thenReturn(emptyList())

        // when
        viewModel.fetchNearByVenues(mockLatitude, mockLongitude)

        // then
        testObserver.assertValueSequence(
            listOf(
                MainState.Uninitialized,
                MainState.Loading,
                MainState.PermissionGranted.Empty
            )
        )
    }

    @Test
    fun `When fails to get the nearby places, Shows error view`() = runTest {
        val testObserver = viewModel.state.test()

        // given
        val mockLatitude = 1.1
        val mockLongitude = 1.1
        Mockito.`when`(
            venueRecommendationsUseCase.getNearByVenues(
                mockLatitude,
                mockLongitude,
                MainViewModel.VENUE_RESULT_LIMIT
            )
        ).thenThrow(IOException::class.java)

        // when
        viewModel.fetchNearByVenues(mockLatitude, mockLongitude)

        // then
        testObserver.assertValueSequence(
            listOf(
                MainState.Uninitialized,
                MainState.Loading,
                MainState.Error.General,
            )
        )
    }

    @Test
    fun `When fails to get the current location, Shows error view`() = runTest {
        val testObserver = viewModel.state.test()

        // given
        val mockLatitude = null
        val mockLongitude = 1.1

        // when
        viewModel.setLocationPermissionGranted(isGranted = true)
        viewModel.fetchNearByVenues(mockLatitude, mockLongitude)

        // then
        testObserver.assertValueSequence(
            listOf(
                MainState.Uninitialized,
                MainState.GetCurrentLocation,
                MainState.Error.CurrentLocationFailed,
            )
        )
    }

    @Test
    fun `When the permission request is rejected, Shows empty view`() = runTest {
        val testObserver = viewModel.state.test()

        // when
        viewModel.setLocationPermissionGranted(isGranted = false)

        // then
        testObserver.assertValueSequence(
            listOf(
                MainState.Uninitialized,
                MainState.PermissionDenied,
            )
        )
    }

    private fun mockResult(): List<VenueResult> = listOf(
        VenueResult(
            categories = listOf(
                Category(Icon("", ""), name = "cate1", id = "123"),
                Category(Icon("", ""), name = "cate2", id = "124")
            ),
            distance = 111,
            geocode = GeoCode(Main(1.0, 1.0)),
            location = Location(
                address = "",
                country = "",
                locality = "",
                neighbourhood = emptyList(),
                postcode = "",
                region = "",
                formatted_address = ""
            ),
            name = "Startbucks",
            timezone = "Asia/Seoul"
        ),
        VenueResult(
            categories = listOf(Category(Icon("", ""), name = "cate1", id = "123")),
            distance = 111,
            geocode = GeoCode(Main(1.0, 1.0)),
            location = Location(
                address = "",
                country = "",
                locality = "",
                neighbourhood = emptyList(),
                postcode = "",
                region = "",
                formatted_address = ""
            ),
            name = "Startbucks",
            timezone = "Asia/Seoul"
        ),
        VenueResult(
            categories = listOf(Category(Icon("", ""), name = "cate5", id = "125")),
            distance = 111,
            geocode = GeoCode(Main(1.0, 1.0)),
            location = Location(
                address = "",
                country = "",
                locality = "",
                neighbourhood = emptyList(),
                postcode = "",
                region = "",
                formatted_address = ""
            ),
            name = "Startbucks",
            timezone = "Asia/Seoul"
        )
    )
}
