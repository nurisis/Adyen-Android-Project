package com.adyen.android.assignment.ui

import com.adyen.android.assignment.api.VenueRecommendationsUseCase
import com.adyen.android.assignment.api.model.GeoCode
import com.adyen.android.assignment.api.model.Location
import com.adyen.android.assignment.api.model.Main
import com.adyen.android.assignment.api.model.VenueResult
import com.adyen.android.assignment.base.ViewModelTest
import com.adyen.android.assignment.ui.state.MainState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito

@ExperimentalCoroutinesApi
class MainViewModelTest : ViewModelTest() {

    private lateinit var viewModel: MainViewModel

    @Mock
    lateinit var venueRecommendationsUseCase: VenueRecommendationsUseCase

    @Before
    fun initViewModel() {
        viewModel = MainViewModel(
            venueRecommendationsUseCase
        )
    }

    @Test
    fun `권한 승인 후, 현재 위치를 받아온 후 주변에 존재하는 리스트를 보여준다`() = runTest {
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
                MainState.PermissionGranted.ShowVenues(list = mockResult, scrollPosition = null)
            )
        )
    }

    @Test
    fun `권한 승인 후, 현재 위치를 받아온 후 주변에 존재하는 장소가 없을 때, 엠티뷰를 보여준다`() = runTest {
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
        viewModel.setLocationPermissionGranted(isGranted = true)
        viewModel.fetchNearByVenues(mockLatitude, mockLongitude)

        // then
        testObserver.assertValueSequence(
            listOf(
                MainState.Uninitialized,
                MainState.GetCurrentLocation,
                MainState.Loading,
                MainState.PermissionGranted.Empty
            )
        )
    }

    @Test
    fun `권한 승인 후, 현재 위치를 받아오는데 실패 했을 때, 현재 위치 오류 화면을 보여준다`() = runTest {
            val testObserver = viewModel.state.test()

            // given
            val mockLatitude = null
            val mockLongitude = 1.1

            // when
            withContext(Dispatchers.Default) {
                viewModel.setLocationPermissionGranted(isGranted = true)
                viewModel.fetchNearByVenues(mockLatitude, mockLongitude)
            }

            // then
            testObserver.assertValueSequence(
                listOf(
                    MainState.Uninitialized,
                    MainState.GetCurrentLocation,
                    MainState.Error.CurrentLocationFail,
                )
            )
        }

    @Test
    fun `권한 요청 거절 시, 거절 엠티뷰를 보여준다`() = runTest {
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
            categories = emptyList(),
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
