package com.wheels.app.features.profile.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.wheels.app.core.network.NetworkMonitor
import com.wheels.app.core.session.UserRole
import com.wheels.app.features.profile.data.local.UserProfileLocalDataSource
import com.wheels.app.features.profile.domain.model.User
import com.wheels.app.features.profile.domain.repository.UserProfileRepository
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class FirebaseUserProfileRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val networkMonitor: NetworkMonitor,
    private val localDataSource: UserProfileLocalDataSource,
    private val ioDispatcher: CoroutineDispatcher
) : UserProfileRepository {

    override fun observeCurrentUserProfile(): Flow<User?> {
        return callbackFlow {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                trySend(null)
                close()
                return@callbackFlow
            }

            val localJob = launch {
                localDataSource.cachedProfile.collect { cachedProfile ->
                    trySend(cachedProfile)
                }
            }

            if (networkMonitor.isOnline()) {
                launch {
                    val freshProfile = runCatching { fetchCurrentProfile(currentUser) }.getOrNull()
                    if (freshProfile != null) {
                        localDataSource.saveProfile(freshProfile)
                    }
                }
            }

            awaitClose {
                localJob.cancel()
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun refreshCurrentUserProfile() {
        withContext(ioDispatcher) {
            val currentUser = firebaseAuth.currentUser ?: return@withContext
            if (!networkMonitor.isOnline()) return@withContext

            runCatching { fetchCurrentProfile(currentUser) }
                .getOrNull()
                ?.let { localDataSource.saveProfile(it) }
        }
    }

    override suspend fun clearCachedProfile() {
        withContext(ioDispatcher) {
            localDataSource.clearProfile()
        }
    }

    private suspend fun fetchCurrentProfile(firebaseUser: FirebaseUser): User {
        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(firebaseUser.uid)
            .get()
            .awaitResult()

        val roles = snapshot.resolveStoredRoles()
        val activeRole = snapshot.resolveActiveRole(roles)

        return User(
            id = firebaseUser.uid,
            fullName = snapshot.getString("fullName")
                .orEmpty()
                .ifBlank { firebaseUser.displayName.orEmpty() },
            email = snapshot.getString("email")
                ?: firebaseUser.email.orEmpty(),
            phone = snapshot.getString("phone").orEmpty(),
            createdAtMillis = snapshot.getTimestamp("createdAt")?.toDate()?.time,
            universityId = snapshot.getString("universityId")
                ?: firebaseUser.email.orEmpty().substringBefore("@").uppercase(Locale.getDefault()),
            rating = snapshot.getDouble("rating") ?: 0.0,
            ridesCompleted = snapshot.getLong("ridesCompleted")?.toInt() ?: 0,
            roles = roles,
            activeRole = activeRole
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.resolveStoredRoles(): Set<UserRole> {
        val explicitRoles = get("roles")
            ?.let { raw -> raw as? List<*> }
            ?.mapNotNull { UserRole.fromStorageValue(it as? String) }
            ?.toSet()
            .orEmpty()

        if (explicitRoles.isNotEmpty()) return explicitRoles

        return setOf(UserRole.fromStorageValue(getString("role")) ?: UserRole.PASSENGER)
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.resolveActiveRole(
        roles: Set<UserRole>
    ): UserRole {
        val explicitActiveRole = UserRole.fromStorageValue(getString("activeRole"))
        return when {
            explicitActiveRole != null && explicitActiveRole in roles -> explicitActiveRole
            UserRole.PASSENGER in roles -> UserRole.PASSENGER
            else -> roles.firstOrNull() ?: UserRole.PASSENGER
        }
    }

    private suspend fun <T> Task<T>.awaitResult(): T {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) }
            addOnFailureListener { exception -> continuation.resumeWithException(exception) }
        }
    }

    private companion object {
        const val USERS_COLLECTION = "users"
    }
}
