package com.wheels.app.features.auth.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.wheels.app.core.common.Resource
import com.wheels.app.features.auth.data.remote.mapper.toProfileUser
import com.wheels.app.features.auth.domain.model.AuthFailure
import com.wheels.app.features.auth.domain.model.AuthUser
import com.wheels.app.features.auth.domain.model.CreateAccountRequest
import com.wheels.app.features.auth.domain.model.ForgotPasswordRequest
import com.wheels.app.features.auth.domain.model.SignInRequest
import com.wheels.app.features.auth.domain.repository.AuthRepository
import com.wheels.app.features.auth.domain.util.buildInstitutionalEmail
import com.wheels.app.features.profile.domain.model.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val ioDispatcher: CoroutineDispatcher
) : AuthRepository {

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

            runCatching {
                val authResult = firebaseAuth
                    .createUserWithEmailAndPassword(institutionalEmail, request.password)
                    .awaitResult()

                val firebaseUser = authResult.user
                    ?: throw IllegalStateException("User registration finished without a Firebase user.")

                val authUser = AuthUser(
                    uid = firebaseUser.uid,
                    email = institutionalEmail,
                    role = DEFAULT_ROLE,
                    fullName = request.fullName.trim()
                )

                try {
                    firestore.collection(USERS_COLLECTION)
                        .document(firebaseUser.uid)
                        .set(
                            mapOf(
                                "fullName" to authUser.fullName,
                                "email" to authUser.email,
                                "role" to authUser.role,
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

                resolveAuthUser(firebaseUser)
            }.fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.toAuthFailure().message) }
            )
        }
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): Resource<Unit> {
        return withContext(ioDispatcher) {
            val institutionalEmail = buildInstitutionalEmail(request.username)

            runCatching {
                firebaseAuth.sendPasswordResetEmail(institutionalEmail).awaitResult()
            }.fold(
                onSuccess = { Resource.Success(Unit) },
                onFailure = { Resource.Error(it.toAuthFailure().message) }
            )
        }
    }

    override suspend fun signOut() {
        withContext(ioDispatcher) {
            firebaseAuth.signOut()
        }
    }

    private suspend fun resolveAuthUser(firebaseUser: FirebaseUser): AuthUser {
        val snapshot = runCatching {
            firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .awaitResult()
        }.getOrNull()

        val fullName = snapshot?.getString("fullName").orEmpty()
        val role = snapshot?.getString("role").orEmpty().ifBlank { DEFAULT_ROLE }

        return AuthUser(
            uid = firebaseUser.uid,
            email = snapshot?.getString("email") ?: firebaseUser.email.orEmpty(),
            role = role,
            fullName = fullName
        )
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
        const val DEFAULT_ROLE = "passenger"
    }
}
