package com.wheels.app.features.rides.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import com.wheels.app.core.common.Resource
import com.wheels.app.core.network.NetworkMonitor
import com.wheels.app.core.trust.domain.repository.DriverRideTrustActionParams
import com.wheels.app.core.trust.domain.repository.DriverTrustRepository
import com.wheels.app.features.rides.data.local.AvailableRidesLocalCache
import com.wheels.app.features.rides.data.local.DriverRidesLocalCache
import com.wheels.app.features.rides.data.local.RideOfflineDao
import com.wheels.app.features.rides.data.local.toDomain
import com.wheels.app.features.rides.data.local.toEntity
import com.wheels.app.features.rides.data.local.toPendingRidePublishEntity
import com.wheels.app.features.rides.data.local.toPublishRideRequest
import com.wheels.app.features.rides.data.local.NearRidesLocalCache
import com.wheels.app.features.rides.data.remote.NearRidesRemoteDataSource
import com.wheels.app.features.rides.domain.model.Booking
import com.wheels.app.features.rides.domain.model.Coordinates
import com.wheels.app.features.rides.domain.model.CreateRideDraft
import com.wheels.app.features.rides.domain.model.NearRidesQuery
import com.wheels.app.features.rides.domain.model.PendingRideAction
import com.wheels.app.features.rides.domain.model.PendingRideActionSyncResult
import com.wheels.app.features.rides.domain.model.PendingRideActionType
import com.wheels.app.features.rides.domain.model.PendingRidePublish
import com.wheels.app.features.rides.domain.model.PublishRideRequest
import com.wheels.app.features.rides.domain.model.DriverRideRecord
import com.wheels.app.features.rides.domain.model.Ride
import com.wheels.app.features.rides.domain.model.RideApplication
import com.wheels.app.features.rides.domain.repository.RideRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Singleton
class RideRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val nearRidesRemoteDataSource: NearRidesRemoteDataSource,
    private val nearRidesLocalCache: NearRidesLocalCache,
    private val availableRidesLocalCache: AvailableRidesLocalCache,
    private val driverRidesLocalCache: DriverRidesLocalCache,
    private val rideOfflineDao: RideOfflineDao,
    private val networkMonitor: NetworkMonitor,
    private val driverTrustRepository: DriverTrustRepository,
    private val ioDispatcher: CoroutineDispatcher
) : RideRepository {

    override fun getAvailableRides(): Flow<List<Ride>> = watchAvailableRides()

    override fun watchAvailableRides(): Flow<List<Ride>> = flow {
        val cached = withContext(ioDispatcher) {
            availableRidesLocalCache.get()
        }

        if (cached != null) {
            emit(cached.rides)
        }

        val isOnline = withContext(ioDispatcher) {
            networkMonitor.isOnline()
        }

        if (!isOnline) {
            if (cached == null) {
                emit(emptyList())
            }
            return@flow
        }

        emitAll(
            observeRemoteAvailableRides()
                .onEach { rides ->
                    withContext(ioDispatcher) {
                        availableRidesLocalCache.put(rides)
                    }
                }
        )
    }

    override fun getNearRides(query: NearRidesQuery): Flow<Resource<List<Ride>>> = flow {
        val cached = withContext(ioDispatcher) {
            nearRidesLocalCache.get(query)
        }

        if (cached != null) {
            emit(Resource.Success(cached.rides))
        } else {
            emit(Resource.Loading)
        }

        val isOnline = withContext(ioDispatcher) {
            networkMonitor.isOnline()
        }

        if (!isOnline) {
            if (cached == null) {
                emit(Resource.Error("No connection. Connect to the internet to find nearby rides."))
            }
            return@flow
        }

        runCatching {
            nearRidesRemoteDataSource.fetchNearRides(query)
        }.onSuccess { freshRides ->
            withContext(ioDispatcher) {
                nearRidesLocalCache.put(query, freshRides)
            }
            emit(Resource.Success(freshRides))
        }.onFailure { throwable ->
            if (cached == null) {
                emit(
                    Resource.Error(
                        message = throwable.message ?: "Near rides are unavailable right now.",
                        throwable = throwable
                    )
                )
            }
        }
    }

    override fun getLatestCachedNearRides(): Flow<Resource<List<Ride>>> = flow {
        val cached = withContext(ioDispatcher) {
            nearRidesLocalCache.getLatest()
        }

        if (cached != null) {
            emit(Resource.Success(cached.rides))
        } else {
            emit(Resource.Error("No saved nearby rides are available yet."))
        }
    }

    override fun observeRide(rideId: String): Flow<Ride?> = callbackFlow {
        val registration = firestore
            .collection(RIDES_COLLECTION)
            .document(rideId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                launch {
                    val baseRide = snapshot?.toAvailableRide()
                    if (baseRide == null) {
                        trySend(null)
                        return@launch
                    }

                    val reliabilityScore = fetchReliabilityScore(baseRide.driverId)
                    trySend(
                        baseRide.copy(
                            reliabilityScore = reliabilityScore ?: baseRide.reliabilityScore
                        )
                    )
                }
            }

        awaitClose { registration.remove() }
    }

    override fun observeDriverRides(driverId: String): Flow<List<DriverRideRecord>> {
        return flow {
            val cached = withContext(ioDispatcher) {
                driverRidesLocalCache.get(driverId)
            }

            if (cached != null) {
                emit(cached.rides)
            }

            val isOnline = withContext(ioDispatcher) {
                networkMonitor.isOnline()
            }

            if (!isOnline) {
                if (cached == null) {
                    emit(emptyList())
                }
                return@flow
            }

            emitAll(
                observeRemoteDriverRides(driverId)
                    .onEach { rides ->
                        withContext(ioDispatcher) {
                            driverRidesLocalCache.put(driverId, rides)
                        }
                    }
            )
        }
    }

    override fun watchCurrentDriverRide(driverId: String): Flow<Ride?> = callbackFlow {
        val registration = firestore
            .collection(RIDES_COLLECTION)
            .whereEqualTo("driverId", driverId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                launch {
                    val currentRide = snapshot?.documents
                        .orEmpty()
                        .mapNotNull { document -> document.toAvailableRide() }
                        .firstOrNull { ride ->
                            ride.status == RIDE_STATUS_OPEN || ride.status == RIDE_STATUS_IN_PROGRESS
                        }

                    trySend(currentRide)
                }
            }

        awaitClose { registration.remove() }
    }

    override fun watchCurrentPassengerRide(passengerId: String): Flow<Ride?> = callbackFlow {
        val registration = firestore
            .collection(RIDES_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                launch {
                    val currentRide = snapshot?.documents
                        .orEmpty()
                        .mapNotNull { document -> document.toAvailableRide() }
                        .firstOrNull { ride ->
                            ride.passengerIds.contains(passengerId) &&
                                isPassengerHomeRideVisible(ride.status)
                        }

                    trySend(currentRide)
                }
            }

        awaitClose { registration.remove() }
    }

    override fun watchRideApplications(rideId: String): Flow<List<RideApplication>> = callbackFlow {
        val registration = firestore
            .collection(RIDES_COLLECTION)
            .document(rideId)
            .collection(APPLICATIONS_SUBCOLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val applications = snapshot?.documents
                    ?.mapNotNull { document -> mapRideApplicationDocument(document.id, document.data ?: emptyMap()) }
                    .orEmpty()
                    .sortedBy { it.appliedAt }

                trySend(applications)
            }

        awaitClose { registration.remove() }
    }

    override fun watchPassengerApplication(
        rideId: String,
        passengerId: String
    ): Flow<RideApplication?> = callbackFlow {
        val registration = firestore
            .collection(RIDES_COLLECTION)
            .document(rideId)
            .collection(APPLICATIONS_SUBCOLLECTION)
            .document(passengerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                trySend(
                    snapshot?.takeIf { it.exists() }
                        ?.let { mapRideApplicationDocument(it.id, it.data ?: emptyMap()) }
                )
            }

        awaitClose { registration.remove() }
    }

    private fun observeRemoteAvailableRides(): Flow<List<Ride>> = callbackFlow {
        val registration = firestore
            .collection(RIDES_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                launch {
                    val rides = snapshot
                        ?.toAvailableRides()
                        .orEmpty()
                        .filter { ride -> ride.status == RIDE_STATUS_OPEN }
                        .filter { it.availableSeats > 0 }

                    val reliabilityScores = fetchReliabilityScores(
                        driverIds = rides.map { it.driverId }.distinct()
                    )

                    val enrichedRides = rides
                        .map { ride ->
                            ride.copy(
                                reliabilityScore = reliabilityScores[ride.driverId]
                                    ?: ride.reliabilityScore
                            )
                        }
                        .sortedWith(
                            compareByDescending<Ride> { it.reliabilityScore }
                                .thenBy { it.departureTime }
                        )

                    trySend(enrichedRides)
                }
            }

        awaitClose { registration.remove() }
    }

    private fun observeRemoteDriverRides(driverId: String): Flow<List<DriverRideRecord>> = callbackFlow {
        val registration = firestore
            .collection(RIDES_COLLECTION)
            .whereEqualTo("driverId", driverId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val rides = snapshot?.documents
                    ?.mapNotNull { document -> mapDriverRideDocument(document.id, document.data ?: emptyMap()) }
                    ?.sortedBy { it.departureAt }
                    .orEmpty()

                trySend(rides)
            }

        awaitClose { registration.remove() }
    }

    override fun observeCreateRideDraft(driverId: String): Flow<CreateRideDraft?> {
        return rideOfflineDao.observeCreateRideDraft(driverId).flowOn(ioDispatcher).map { it?.toDomain() }
    }

    override fun observePendingRideActions(driverId: String): Flow<List<PendingRideAction>> {
        return rideOfflineDao.observePendingRideActions(driverId)
            .flowOn(ioDispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun observePendingRidePublishes(driverId: String): Flow<List<PendingRidePublish>> {
        return rideOfflineDao.observePendingRidePublishes(driverId)
            .flowOn(ioDispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun saveCreateRideDraft(draft: CreateRideDraft) {
        withContext(ioDispatcher) {
            rideOfflineDao.upsertCreateRideDraft(draft.toEntity())
        }
    }

    override suspend fun clearCreateRideDraft(driverId: String) {
        withContext(ioDispatcher) {
            rideOfflineDao.clearCreateRideDraft(driverId)
        }
    }

    override suspend fun enqueuePendingRideAction(action: PendingRideAction) {
        withContext(ioDispatcher) {
            rideOfflineDao.insertPendingRideAction(action.toEntity())
        }
    }

    override suspend fun enqueueRidePublish(request: PublishRideRequest) {
        withContext(ioDispatcher) {
            rideOfflineDao.insertPendingRidePublish(request.toPendingRidePublishEntity())
        }
    }

    override suspend fun deletePendingRidePublish(id: String) {
        withContext(ioDispatcher) {
            rideOfflineDao.deletePendingRidePublish(id)
        }
    }

    override suspend fun syncPendingRidePublishes(driverId: String): Int {
        return withContext(ioDispatcher) {
            val pendingPublishes = rideOfflineDao.getPendingRidePublishes(driverId)
            var syncedCount = 0

            for (pendingPublish in pendingPublishes) {
                try {
                    publishRide(pendingPublish.toPublishRideRequest())
                    rideOfflineDao.deletePendingRidePublish(pendingPublish.id)
                    syncedCount += 1
                } catch (throwable: Throwable) {
                    rideOfflineDao.markPendingRidePublishFailed(
                        id = pendingPublish.id,
                        lastError = throwable.message ?: "We could not sync this ride yet."
                    )
                    break
                }
            }

            syncedCount
        }
    }

    override suspend fun syncPendingRideActions(driverId: String): List<PendingRideActionSyncResult> {
        return withContext(ioDispatcher) {
            val pendingActions = rideOfflineDao.getPendingRideActions(driverId)
            val syncedActions = mutableListOf<PendingRideActionSyncResult>()

            for (pendingAction in pendingActions) {
                try {
                    syncedActions += executePendingRideAction(pendingAction.toDomain())
                    rideOfflineDao.deletePendingRideAction(pendingAction.rideId)
                } catch (throwable: Throwable) {
                    rideOfflineDao.markPendingRideActionFailed(
                        rideId = pendingAction.rideId,
                        lastError = throwable.message ?: "We could not sync this ride action yet."
                    )
                    break
                }
            }

            syncedActions
        }
    }

    override suspend fun createRide(request: PublishRideRequest): String {
        val rideRef = firestore.collection(RIDES_COLLECTION).document()
        val rideId = rideRef.id

        rideRef.set(buildRideCreationData(request)).awaitResult()
        return rideId
    }

    override suspend fun publishRide(request: PublishRideRequest): String {
        return createRide(request)
    }

    override suspend fun applyToRide(
        rideId: String,
        passengerId: String,
        passengerName: String,
        passengerEmail: String
    ): RideApplication {
        val rideRef = firestore.collection(RIDES_COLLECTION).document(rideId)
        val applicationRef = rideRef.collection(APPLICATIONS_SUBCOLLECTION).document(passengerId)
        val paymentMirrorRef = firestore.collection(PAYMENTS_COLLECTION)
            .document(rideId)
            .collection(PASSENGERS_SUBCOLLECTION)
            .document(passengerId)

        return firestore.runTransaction { transaction ->
            val rideSnapshot = transaction.get(rideRef)
            if (!rideSnapshot.exists()) {
                throw IllegalStateException("Ride not found.")
            }

            val rideData = rideSnapshot.data ?: emptyMap()
            val decision = decideRideApplication(
                rideId = rideId,
                rideData = rideData,
                passengerId = passengerId,
                passengerName = passengerName,
                passengerEmail = passengerEmail,
                existingApplicationExists = transaction.get(applicationRef).exists()
            )

            when (decision) {
                ApplyRideDecision.NoOp -> {
                    val existing = transaction.get(applicationRef)
                    mapRideApplicationDocument(existing.id, existing.data ?: emptyMap())
                        ?: throw IllegalStateException("Application already exists but could not be read.")
                }
                is ApplyRideDecision.Apply -> {
                    if (decision.application.status == APPLICATION_STATUS_REJECTED) {
                        throw IllegalStateException(
                            when (decision.application.statusDetail) {
                                "driver_cannot_apply" -> "Drivers cannot apply to their own ride."
                                "ride_not_open" -> "This ride is no longer open."
                                "ride_is_full" -> "This ride is already full."
                                else -> "We could not apply to this ride."
                            }
                        )
                    }

                    val currentAvailableSeats = readInt(rideData, "availableSeats")
                        ?: readInt(rideData, "totalSeats")
                        ?: readInt(rideData, "seats")
                        ?: 0
                    if (currentAvailableSeats <= 0) {
                        throw IllegalStateException("This ride is already full.")
                    }

                    transaction.update(
                        rideRef,
                        mapOf(
                            "availableSeats" to currentAvailableSeats - 1,
                            "passengerIds" to FieldValue.arrayUnion(passengerId),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )

                    val applicationData = buildApplicationDocumentData(decision.application)
                    transaction.set(applicationRef, applicationData)
                    transaction.set(
                        paymentMirrorRef,
                        buildPassengerPaymentMirrorData(
                            rideId = rideId,
                            passengerId = passengerId,
                            paymentMethodId = decision.paymentMethodId,
                            paymentStatus = decision.paymentStatus,
                            paymentStatusSource = decision.paymentStatusSource,
                            isPaymentLocked = false,
                            status = decision.paymentMirrorStatus,
                            statusDetail = decision.paymentMirrorStatusDetail
                        )
                    )
                    decision.application
                }
            }
        }.awaitResult()
    }

    override suspend fun updateRideStatus(rideId: String, status: String) {
        val rideRef = firestore.collection(RIDES_COLLECTION).document(rideId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(rideRef)
            if (!snapshot.exists()) {
                throw IllegalStateException("Ride not found.")
            }

            val currentStatus = normalizeRideReadStatus(snapshot.getString("status"))
            when (val decision = validateRideStatusTransition(currentStatus, status)) {
                RideStatusTransitionDecision.Allowed -> {
                    transaction.update(rideRef, buildRideStatusUpdate(status))
                }
                is RideStatusTransitionDecision.Rejected -> {
                    throw IllegalStateException(decision.reason)
                }
            }
        }.awaitResult()
    }

    override suspend fun finishRide(rideId: String) {
        val rideRef = firestore.collection(RIDES_COLLECTION).document(rideId)
        val applicationsRef = rideRef.collection(APPLICATIONS_SUBCOLLECTION)

        val rideSnapshot = rideRef.get().awaitResult()
        if (!rideSnapshot.exists()) {
            throw IllegalStateException("Ride not found.")
        }

        val applicationsSnapshot = applicationsRef.get().awaitResult()
        val passengerStates = applicationsSnapshot.documents.mapNotNull { document ->
            mapRideApplicationDocument(document.id, document.data ?: emptyMap())
        }

        val batch = firestore.batch()
        passengerStates.forEach { application ->
            val finalPaymentStatus = if (application.status == APPLICATION_STATUS_REJECTED) {
                PAYMENT_STATUS_UNPAID
            } else {
                PAYMENT_STATUS_PAID
            }

            batch.set(
                firestore.collection(PAYMENTS_COLLECTION)
                    .document(rideId)
                    .collection(PASSENGERS_SUBCOLLECTION)
                    .document(application.passengerId),
                buildPassengerPaymentMirrorData(
                    rideId = rideId,
                    passengerId = application.passengerId,
                    paymentMethodId = application.paymentMethod,
                    paymentStatus = finalPaymentStatus,
                    paymentStatusSource = PAYMENT_STATUS_SOURCE_RIDE_FINISHED,
                    isPaymentLocked = true,
                    status = if (finalPaymentStatus == PAYMENT_STATUS_PAID) {
                        PAYMENT_MIRROR_STATUS_APPROVED
                    } else {
                        PAYMENT_MIRROR_STATUS_REJECTED
                    },
                    statusDetail = "ride_completed"
                )
            )

            batch.update(
                applicationsRef.document(application.passengerId),
                mapOf(
                    "status" to if (finalPaymentStatus == PAYMENT_STATUS_PAID) {
                        APPLICATION_STATUS_APPROVED
                    } else {
                        APPLICATION_STATUS_REJECTED
                    },
                    "paymentStatus" to finalPaymentStatus,
                    "isPaymentLocked" to true,
                    "paymentStatusSource" to PAYMENT_STATUS_SOURCE_RIDE_FINISHED,
                    "statusDetail" to "ride_completed",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        batch.update(rideRef, buildRideFinishUpdate())
        batch.commit().awaitResult()
    }

    override suspend fun updatePassengerPaymentStatus(
        rideId: String,
        passengerId: String,
        paymentMethodId: String,
        paymentStatus: String,
        paymentStatusSource: String,
        isPaymentLocked: Boolean,
        status: String,
        statusDetail: String?
    ) {
        val rideRef = firestore.collection(RIDES_COLLECTION).document(rideId)
        val applicationRef = rideRef.collection(APPLICATIONS_SUBCOLLECTION).document(passengerId)

        firestore.runTransaction { transaction ->
            updatePassengerPaymentStatusInternal(
                transaction = transaction,
                rideId = rideId,
                passengerId = passengerId,
                paymentMethodId = paymentMethodId,
                paymentStatus = paymentStatus,
                paymentStatusSource = paymentStatusSource,
                isPaymentLocked = isPaymentLocked,
                status = status,
                statusDetail = statusDetail
            )

            transaction.set(
                applicationRef,
                mapOf(
                    "rideId" to rideId,
                    "passengerId" to passengerId,
                    "paymentMethod" to paymentMethodId,
                    "paymentStatus" to paymentStatus,
                    "paymentStatusSource" to paymentStatusSource,
                    "isPaymentLocked" to isPaymentLocked,
                    "status" to APPLICATION_STATUS_APPLIED,
                    "statusDetail" to statusDetail,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
        }.awaitResult()
    }

    private fun updatePassengerPaymentStatusInternal(
        transaction: com.google.firebase.firestore.Transaction,
        rideId: String,
        passengerId: String,
        paymentMethodId: String,
        paymentStatus: String,
        paymentStatusSource: String,
        isPaymentLocked: Boolean,
        status: String,
        statusDetail: String?
    ) {
        val paymentMirrorRef = firestore.collection(PAYMENTS_COLLECTION)
            .document(rideId)
            .collection(PASSENGERS_SUBCOLLECTION)
            .document(passengerId)

        transaction.set(
            paymentMirrorRef,
            buildPassengerPaymentMirrorData(
                rideId = rideId,
                passengerId = passengerId,
                paymentMethodId = paymentMethodId,
                paymentStatus = paymentStatus,
                paymentStatusSource = paymentStatusSource,
                isPaymentLocked = isPaymentLocked,
                status = status,
                statusDetail = statusDetail
            )
        )
    }

    override suspend fun deleteDriverRide(rideId: String) {
        firestore.collection(RIDES_COLLECTION).document(rideId).delete().awaitResult()
    }

    private suspend fun executePendingRideAction(action: PendingRideAction): PendingRideActionSyncResult {
        return when (action.actionType) {
            PendingRideActionType.START -> {
                driverTrustRepository.startRide(action.toTrustActionParams())
                PendingRideActionSyncResult(
                    rideId = action.rideId,
                    actionType = action.actionType,
                    previousTrustScore = null,
                    newTrustScore = null
                )
            }
            PendingRideActionType.COMPLETE -> {
                val notice =
                    driverTrustRepository.completeRideAndAwaitTrustUpdate(action.toTrustActionParams())
                PendingRideActionSyncResult(
                    rideId = action.rideId,
                    actionType = action.actionType,
                    previousTrustScore = action.previousTrustScore,
                    newTrustScore = notice.newScore
                )
            }
            PendingRideActionType.CANCEL -> {
                val notice =
                    driverTrustRepository.cancelRideAndAwaitTrustUpdate(action.toTrustActionParams())
                PendingRideActionSyncResult(
                    rideId = action.rideId,
                    actionType = action.actionType,
                    previousTrustScore = action.previousTrustScore,
                    newTrustScore = notice.newScore
                )
            }
            PendingRideActionType.DELETE -> {
                deleteDriverRide(action.rideId)
                PendingRideActionSyncResult(
                    rideId = action.rideId,
                    actionType = action.actionType,
                    previousTrustScore = null,
                    newTrustScore = null
                )
            }
        }
    }

    override suspend fun bookRide(rideId: String, seats: Int): Booking =
        Booking(
            id = "b_001",
            rideId = rideId,
            passengerId = "u_002",
            seatsReserved = seats,
            status = "PENDING"
        )

    private suspend fun QuerySnapshot.toAvailableRides(): List<Ride> {
        return documents.mapNotNull { document -> mapRideDocument(document.id, document.data ?: emptyMap()) }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toAvailableRide(): Ride? {
        return mapRideDocument(id, data ?: emptyMap())
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toDriverRideRecord(): DriverRideRecord? {
        return mapDriverRideDocument(id, data ?: emptyMap())
    }

    private suspend fun <T> Task<T>.awaitResult(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) }
            addOnFailureListener { exception -> continuation.resumeWithException(exception) }
        }
    }

    private suspend fun fetchReliabilityScores(driverIds: List<String>): Map<String, Int> {
        return driverIds.associateWith { driverId ->
            fetchReliabilityScore(driverId) ?: DEFAULT_RELIABILITY_SCORE
        }
    }

    private suspend fun fetchReliabilityScore(driverId: String): Int? {
        if (driverId.isBlank()) return null

        val snapshot = firestore
            .collection(TRUST_SCORES_COLLECTION)
            .document(driverId)
            .get()
            .awaitResult()

        return snapshot.getLong("reliabilityScore")?.toInt()
    }

    private companion object {
        const val RIDES_COLLECTION = "rides"
        const val TRUST_SCORES_COLLECTION = "trustScores"
        const val PAYMENTS_COLLECTION = "payments"
        const val APPLICATIONS_SUBCOLLECTION = "applications"
        const val PASSENGERS_SUBCOLLECTION = "passengers"
        const val DEFAULT_RIDE_DURATION_MINUTES = 30
        const val DEFAULT_RELIABILITY_SCORE = 100
    }
}

private fun PendingRideAction.toTrustActionParams(): DriverRideTrustActionParams {
    return DriverRideTrustActionParams(
        rideId = rideId,
        driverId = driverId,
        scheduledStartAtMillis = scheduledStartAtMillis
    )
}
