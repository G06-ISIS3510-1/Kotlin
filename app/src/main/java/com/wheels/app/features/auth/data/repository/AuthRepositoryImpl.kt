package com.wheels.app.features.auth.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.wheels.app.core.common.Resource
import com.wheels.app.core.session.UserRole
import com.wheels.app.features.auth.data.remote.mapper.toProfileUser
import com.wheels.app.features.auth.domain.model.AuthFailure
import com.wheels.app.features.auth.domain.model.AuthUser
import com.wheels.app.features.auth.domain.model.CreateAccountRequest
import com.wheels.app.features.auth.domain.model.ForgotPasswordRequest
import com.wheels.app.features.auth.domain.model.SignInRequest
import com.wheels.app.features.auth.domain.repository.AuthRepository
import com.wheels.app.features.auth.domain.util.buildInstitutionalEmail
import com.wheels.app.features.profile.domain.model.User
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val ioDispatcher: CoroutineDispatcher
) : AuthRepository {

    private val loginHistoryTimestamps = Collections.synchronizedList(mutableListOf<Long>())

    override fun observeAuthSession(): Flow<AuthUser?> {
        return callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                val user = auth.currentUser
                if (user == null) {
                    trySend(null)
                    return@AuthStateListener
                }

                launch(ioDispatcher) {
                    val authUser = runCatching { resolveAuthUser(user) }.getOrNull()
                    trySend(authUser)
                }
            }

            firebaseAuth.addAuthStateListener(listener)
            awaitClose { firebaseAuth.removeAuthStateListener(listener) }
        }.flowOn(ioDispatcher)
    }

    override fun getCurrentUser(): Flow<User?> {
        return observeAuthSession().map { authUser -> authUser?.toProfileUser() }
    }

    override fun getLoginHistory(): List<Long> = loginHistoryTimestamps.toList()

    override suspend fun restoreSession(): AuthUser? = withContext(ioDispatcher) {
        val currentUser = firebaseAuth.currentUser ?: return@withContext null

        return@withContext runCatching {
            currentUser.reload().awaitResult()
            val refreshedUser = firebaseAuth.currentUser ?: return@runCatching null
            resolveAuthUser(refreshedUser)
        }.getOrElse {
            firebaseAuth.signOut()
            null
        }
    }

    override suspend fun createAccount(request: CreateAccountRequest): Resource<AuthUser> {
        return withContext(ioDispatcher) {
            val institutionalEmail = buildInstitutionalEmail(request.username)
            val roles = request.roles.ifEmpty { setOf(UserRole.PASSENGER) }
            val activeRole = roles.resolveInitialActiveRole()

            runCatching {
                val authResult = firebaseAuth
                    .createUserWithEmailAndPassword(institutionalEmail, request.password)
                    .awaitResult()

                val firebaseUser = authResult.user
                    ?: throw IllegalStateException("User registration finished without a Firebase user.")

                val authUser = AuthUser(
                    uid = firebaseUser.uid,
                    email = institutionalEmail,
                    fullName = request.fullName.trim(),
                    phone = request.phone.trim(),
                    createdAtMillis = null,
                    roles = roles,
                    activeRole = activeRole
                )

                try {
                    firestore.collection(USERS_COLLECTION)
                        .document(firebaseUser.uid)
                        .set(
                            mapOf(
                                "fullName" to authUser.fullName,
                                "email" to authUser.email,
                                "phone" to authUser.phone,
                                "roles" to authUser.roles.map { it.storageValue },
                                "activeRole" to authUser.activeRole.storageValue,
                                "photoUrl" to "",
                                "createdAt" to FieldValue.serverTimestamp(),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                        .awaitResult()
                } catch (firestoreException: Exception) {
                    runCatching { firebaseUser.delete().awaitResult() }
                    firebaseAuth.signOut()
                    throw firestoreException
                }

                loginHistoryTimestamps.add(System.currentTimeMillis())
                authUser
            }.fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.toAuthFailure().message) }
            )
        }
    }

    override suspend fun signIn(request: SignInRequest): Resource<AuthUser> {
        return withContext(ioDispatcher) {
            val institutionalEmail = buildInstitutionalEmail(request.username)

            runCatching {
                val authResult = firebaseAuth
                    .signInWithEmailAndPassword(institutionalEmail, request.password)
                    .awaitResult()

                val firebaseUser = authResult.user
                    ?: throw IllegalStateException("Sign in finished without a Firebase user.")

                resolveAuthUser(firebaseUser).also {
                    loginHistoryTimestamps.add(System.currentTimeMillis())
                }
            }.fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.toAuthFailure().message) }
            )
        }
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): Resource<Unit> {
        return withContext(ioDispatcher) {
            val institutionalEmail = buildInstitutionalEmail(request.username)
            val normalizedUsername = request.username.trim().substringBefore("@").lowercase()

            runCatching {
                firebaseAuth.sendPasswordResetEmail(institutionalEmail).awaitResult()
                runCatching {
                    firestore.collection(PASSWORD_RESET_REQUESTS_COLLECTION)
                        .add(
                            mapOf(
                                "username" to normalizedUsername,
                                "email" to institutionalEmail,
                                "status" to "email_sent",
                                "requestedAt" to FieldValue.serverTimestamp(),
                                "source" to "android_app"
                            )
                        )
                        .awaitResult()
                }
            }.fold(
                onSuccess = { Resource.Success(Unit) },
                onFailure = {
                    runCatching {
                        firestore.collection(PASSWORD_RESET_REQUESTS_COLLECTION)
                            .add(
                                mapOf(
                                    "username" to normalizedUsername,
                                    "email" to institutionalEmail,
                                    "status" to "failed",
                                    "requestedAt" to FieldValue.serverTimestamp(),
                                    "failureMessage" to it.toAuthFailure().message,
                                    "source" to "android_app"
                                )
                            )
                            .awaitResult()
                    }
                    Resource.Error(it.toAuthFailure().message)
                }
            )
        }
    }

    override suspend fun switchActiveRole(role: UserRole): Resource<AuthUser> {
        return withContext(ioDispatcher) {
            runCatching {
                val currentUser = firebaseAuth.currentUser
                    ?: throw IllegalStateException("No authenticated user found.")
                val currentAuthUser = resolveAuthUser(currentUser)
                if (role !in currentAuthUser.roles) {
                    throw IllegalStateException("You are not registered as ${role.displayName.lowercase()} yet.")
                }

                updateUserRolesDocument(
                    uid = currentUser.uid,
                    roles = currentAuthUser.roles,
                    activeRole = role
                )

                resolveAuthUser(currentUser)
            }.fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.toAuthFailure().message) }
            )
        }
    }

    override suspend fun registerAdditionalRole(role: UserRole, password: String): Resource<AuthUser> {
        return withContext(ioDispatcher) {
            runCatching {
                val currentUser = firebaseAuth.currentUser
                    ?: throw IllegalStateException("No authenticated user found.")
                val currentEmail = currentUser.email.orEmpty()
                if (currentEmail.isBlank()) {
                    throw IllegalStateException("Your account email is unavailable.")
                }
                if (password.isBlank()) {
                    throw IllegalArgumentException("Enter your password to confirm this role change.")
                }

                currentUser.reauthenticate(
                    EmailAuthProvider.getCredential(currentEmail, password)
                ).awaitResult()

                val currentAuthUser = resolveAuthUser(currentUser)
                val updatedRoles = currentAuthUser.roles + role
                updateUserRolesDocument(
                    uid = currentUser.uid,
                    roles = updatedRoles,
                    activeRole = role
                )

                resolveAuthUser(currentUser)
            }.fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.toAuthFailure().message) }
            )
        }
    }

    override suspend fun signOut() {
        withContext(ioDispatcher) {
            firebaseAuth.signOut()
        }
    }

    private suspend fun updateUserRolesDocument(
        uid: String,
        roles: Set<UserRole>,
        activeRole: UserRole
    ) {
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .set(
                mapOf(
                    "roles" to roles.map { it.storageValue },
                    "activeRole" to activeRole.storageValue,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
            .awaitResult()
    }

    private suspend fun resolveAuthUser(firebaseUser: FirebaseUser): AuthUser {
        val snapshot = runCatching {
            firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .awaitResult()
        }.getOrNull()

        val fullName = snapshot?.getString("fullName").orEmpty()
        val resolvedRoles = snapshot.resolveStoredRoles()
        val activeRole = snapshot.resolveActiveRole(resolvedRoles)

        return AuthUser(
            uid = firebaseUser.uid,
            email = snapshot?.getString("email") ?: firebaseUser.email.orEmpty(),
            fullName = fullName,
            phone = snapshot?.getString("phone").orEmpty(),
            createdAtMillis = snapshot?.getTimestamp("createdAt")?.toDate()?.time,
            roles = resolvedRoles,
            activeRole = activeRole
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot?.resolveStoredRoles(): Set<UserRole> {
        val explicitRoles = this
            ?.get("roles")
            ?.let { raw -> raw as? List<*> }
            ?.mapNotNull { UserRole.fromStorageValue(it as? String) }
            ?.toSet()
            .orEmpty()

        if (explicitRoles.isNotEmpty()) {
            return explicitRoles
        }

        return setOf(
            UserRole.fromStorageValue(this?.getString("role")) ?: UserRole.PASSENGER
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot?.resolveActiveRole(
        roles: Set<UserRole>
    ): UserRole {
        val explicitActiveRole = UserRole.fromStorageValue(this?.getString("activeRole"))
        return when {
            explicitActiveRole != null && explicitActiveRole in roles -> explicitActiveRole
            UserRole.PASSENGER in roles -> UserRole.PASSENGER
            else -> roles.firstOrNull() ?: UserRole.PASSENGER
        }
    }

    private fun Set<UserRole>.resolveInitialActiveRole(): UserRole {
        return when {
            UserRole.PASSENGER in this -> UserRole.PASSENGER
            else -> firstOrNull() ?: UserRole.PASSENGER
        }
    }

    private fun Throwable.toAuthFailure(): AuthFailure {
        return when (this) {
            is FirebaseNetworkException -> AuthFailure.NetworkError
            is FirebaseAuthException -> when (errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> AuthFailure.EmailAlreadyInUse
                "ERROR_INVALID_EMAIL" -> AuthFailure.InvalidEmail
                "ERROR_WEAK_PASSWORD" -> AuthFailure.WeakPassword
                "ERROR_USER_NOT_FOUND",
                "ERROR_WRONG_PASSWORD",
                "ERROR_INVALID_CREDENTIAL" -> AuthFailure.InvalidCredentials
                "ERROR_NETWORK_REQUEST_FAILED" -> AuthFailure.NetworkError
                else -> AuthFailure.Unknown(message)
            }
            is IllegalArgumentException -> AuthFailure.Unknown(message)
            is IllegalStateException -> AuthFailure.Unknown(message)
            else -> AuthFailure.Unknown(message)
        }
    }

    private suspend fun <T> Task<T>.awaitResult(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) }
            addOnFailureListener { exception -> continuation.resumeWithException(exception) }
        }
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val PASSWORD_RESET_REQUESTS_COLLECTION = "passwordResetRequests"
    }
}
