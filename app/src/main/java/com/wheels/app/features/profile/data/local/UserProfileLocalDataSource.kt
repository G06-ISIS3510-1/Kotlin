package com.wheels.app.features.profile.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wheels.app.core.session.UserRole
import com.wheels.app.features.profile.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.userProfileDataStore by preferencesDataStore(name = "user_profile_cache")

@Singleton
class UserProfileLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val cachedProfile: Flow<User?> = context.userProfileDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences -> preferences.toUserOrNull() }

    suspend fun saveProfile(user: User) {
        context.userProfileDataStore.edit { preferences ->
            preferences[Keys.ID] = user.id
            preferences[Keys.FULL_NAME] = user.fullName
            preferences[Keys.EMAIL] = user.email
            preferences[Keys.PHONE] = user.phone
            preferences[Keys.CREATED_AT_MILLIS] = user.createdAtMillis ?: -1L
            preferences[Keys.UNIVERSITY_ID] = user.universityId
            preferences[Keys.RATING] = user.rating
            preferences[Keys.RIDES_COMPLETED] = user.ridesCompleted.toLong()
            preferences[Keys.ROLES] = user.roles.joinToString(",") { it.storageValue }
            preferences[Keys.ACTIVE_ROLE] = user.activeRole.storageValue
        }
    }

    suspend fun clearProfile() {
        context.userProfileDataStore.edit { it.clear() }
    }

    private fun Preferences.toUserOrNull(): User? {
        val id = this[Keys.ID].orEmpty()
        if (id.isBlank()) return null

        val roles = this[Keys.ROLES]
            .orEmpty()
            .split(",")
            .mapNotNull { value -> UserRole.fromStorageValue(value.trim()) }
            .toSet()
            .ifEmpty { setOf(UserRole.PASSENGER) }

        return User(
            id = id,
            fullName = this[Keys.FULL_NAME].orEmpty(),
            email = this[Keys.EMAIL].orEmpty(),
            phone = this[Keys.PHONE].orEmpty(),
            createdAtMillis = this[Keys.CREATED_AT_MILLIS]
                ?.takeIf { it >= 0L },
            universityId = this[Keys.UNIVERSITY_ID].orEmpty(),
            rating = this[Keys.RATING] ?: 0.0,
            ridesCompleted = (this[Keys.RIDES_COMPLETED] ?: 0L).toInt(),
            roles = roles,
            activeRole = UserRole.fromStorageValue(this[Keys.ACTIVE_ROLE])
                ?.takeIf { it in roles }
                ?: roles.first()
        )
    }

    private companion object Keys {
        val ID = stringPreferencesKey("id")
        val FULL_NAME = stringPreferencesKey("full_name")
        val EMAIL = stringPreferencesKey("email")
        val PHONE = stringPreferencesKey("phone")
        val CREATED_AT_MILLIS = longPreferencesKey("created_at_millis")
        val UNIVERSITY_ID = stringPreferencesKey("university_id")
        val RATING = doublePreferencesKey("rating")
        val RIDES_COMPLETED = longPreferencesKey("rides_completed")
        val ROLES = stringPreferencesKey("roles")
        val ACTIVE_ROLE = stringPreferencesKey("active_role")
    }
}
