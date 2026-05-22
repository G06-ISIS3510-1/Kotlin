package com.wheels.app.features.favoriteDrivers.data.remote

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wheels.app.features.favoriteDrivers.domain.model.FavoriteDriver
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class FavoriteDriverRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun syncFavorite(driver: FavoriteDriver) = withContext(ioDispatcher) {
        val passengerId = firebaseAuth.currentUser?.uid ?: LOCAL_PASSENGER_ID
        val data = mapOf(
            "driverId" to driver.driverId,
            "driverName" to driver.driverName,
            "rating" to driver.rating,
            "trustScore" to driver.trustScore,
            "completedRides" to driver.completedRides,
            "profileImageUrl" to driver.profileImageUrl,
            "savedAt" to driver.savedAt
        )

        Tasks.await(
            firestore
                .collection("users")
                .document(passengerId)
                .collection("favorite_drivers")
                .document(driver.driverId)
                .set(data)
        )
    }

    suspend fun deleteFavorite(driverId: String) = withContext(ioDispatcher) {
        val passengerId = firebaseAuth.currentUser?.uid ?: LOCAL_PASSENGER_ID
        Tasks.await(
            firestore
                .collection("users")
                .document(passengerId)
                .collection("favorite_drivers")
                .document(driverId)
                .delete()
        )
    }

    companion object {
        private const val LOCAL_PASSENGER_ID = "local_passenger"
    }
}
