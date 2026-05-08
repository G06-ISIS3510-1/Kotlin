package com.wheels.app.features.rides.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.wheels.app.features.reviews.data.local.DriverReviewsLocalCache
import com.wheels.app.features.reviews.data.local.ReviewsCacheSizer
import com.wheels.app.features.reviews.domain.model.DriverReviewsFeed
import com.wheels.app.features.reviews.domain.model.DriverReviewSummary
import com.wheels.app.features.reviews.domain.model.RideReview
import com.wheels.app.features.reviews.domain.model.SubmitRideReviewRequest
import com.wheels.app.features.reviews.domain.repository.RideReviewRepository
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DriverReviewsViewModelTest {

    private val mainDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `reopening driver reviews shows cached content immediately then refreshes from network`() = runTest {
        val driverId = "driver-1"
        val cachedReviews = listOf(sampleReview(driverId, "passenger-1", 5))
        val freshReviews = listOf(
            sampleReview(driverId, "passenger-1", 5),
            sampleReview(driverId, "passenger-2", 4)
        )
        val cache = DriverReviewsLocalCache(FixedReviewsCacheSizer(4)).apply {
            put(driverId, cachedReviews)
        }
        val repository = FakeRideReviewRepository()
        val viewModel = DriverReviewsViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "driverId" to driverId,
                    "driverName" to "Mauricio Urrego"
                )
            ),
            reviewRepository = repository,
            reviewsLocalCache = cache
        )

        assertEquals(cachedReviews, viewModel.uiState.value.reviews)
        assertTrue(viewModel.uiState.value.isShowingCachedContent)
        assertTrue(viewModel.uiState.value.isRefreshing)
        assertFalse(viewModel.uiState.value.isLoading)

        repository.feed.emit(DriverReviewsFeed.Fresh(freshReviews))

        assertEquals(freshReviews, viewModel.uiState.value.reviews)
        assertFalse(viewModel.uiState.value.isShowingCachedContent)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `network failure after cache hit keeps cached reviews visible`() = runTest {
        val driverId = "driver-1"
        val cachedReviews = listOf(sampleReview(driverId, "passenger-1", 5))
        val cache = DriverReviewsLocalCache(FixedReviewsCacheSizer(4)).apply {
            put(driverId, cachedReviews)
        }
        val repository = FakeRideReviewRepository()
        val viewModel = DriverReviewsViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "driverId" to driverId,
                    "driverName" to "Mauricio Urrego"
                )
            ),
            reviewRepository = repository,
            reviewsLocalCache = cache
        )

        repository.feed.emit(DriverReviewsFeed.RefreshError("Reviews temporarily unavailable"))

        assertEquals(cachedReviews, viewModel.uiState.value.reviews)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals("Reviews temporarily unavailable", viewModel.uiState.value.errorMessage)
    }

    private fun sampleReview(driverId: String, passengerId: String, stars: Int): RideReview {
        return RideReview(
            reviewId = "${driverId}_${passengerId}",
            rideId = "",
            driverId = driverId,
            driverName = "Driver",
            passengerId = passengerId,
            passengerName = "Passenger",
            stars = stars,
            comment = "Great ride",
            createdAt = Instant.EPOCH
        )
    }

    private class FakeRideReviewRepository : RideReviewRepository {
        val feed = MutableSharedFlow<DriverReviewsFeed>(extraBufferCapacity = 4)

        override fun observeDriverReviews(driverId: String): Flow<DriverReviewsFeed> = feed.asSharedFlow()

        override fun observeDriverReviewSummaries(): Flow<Map<String, DriverReviewSummary>> = emptyFlow()

        override suspend fun submitReview(request: SubmitRideReviewRequest): RideReview {
            return RideReview(
                reviewId = "${request.driverId}_${request.passengerId}",
                rideId = request.rideId,
                driverId = request.driverId,
                driverName = request.driverName,
                passengerId = request.passengerId,
                passengerName = request.passengerName,
                stars = request.stars,
                comment = request.comment,
                createdAt = Instant.EPOCH
            )
        }
    }

    private class FixedReviewsCacheSizer(
        private val entries: Int
    ) : ReviewsCacheSizer {
        override fun maxEntries(): Int = entries
    }
}
