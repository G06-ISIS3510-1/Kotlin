package com.wheels.app.features.favoriteDrivers.sync

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wheels.app.core.database.WheelsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FavoriteDriverSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        runCatching {
            FirebaseApp.initializeApp(applicationContext)
            val database = Room.databaseBuilder(
                applicationContext,
                WheelsDatabase::class.java,
                DATABASE_NAME
            ).fallbackToDestructiveMigration().build()
            val dao = database.favoriteDriverDao()
            val firestore = FirebaseFirestore.getInstance()
            val passengerId = FirebaseAuth.getInstance().currentUser?.uid ?: LOCAL_PASSENGER_ID

            dao.getPendingSyncDrivers().forEach { driver ->
                val remoteDocument = firestore
                    .collection("users")
                    .document(passengerId)
                    .collection("favorite_drivers")
                    .document(driver.driverId)

                if (driver.pendingDelete) {
                    Tasks.await(remoteDocument.delete())
                    dao.deleteFavoriteDriver(driver.driverId)
                } else {
                    val data = mapOf(
                        "driverId" to driver.driverId,
                        "driverName" to driver.driverName,
                        "rating" to driver.rating,
                        "trustScore" to driver.trustScore,
                        "completedRides" to driver.completedRides,
                        "profileImageUrl" to driver.profileImageUrl,
                        "savedAt" to driver.savedAt
                    )
                    Tasks.await(remoteDocument.set(data))
                    dao.markSynced(driver.driverId)
                }
            }

            database.close()
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }

    companion object {
        const val WORK_NAME = "favorite_driver_sync"
        private const val DATABASE_NAME = "wheels.db"
        private const val LOCAL_PASSENGER_ID = "local_passenger"
    }
}
