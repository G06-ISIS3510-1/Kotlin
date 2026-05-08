package com.wheels.app.features.auth.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.wheels.app.core.session.UserRole
import com.wheels.app.features.auth.domain.model.AuthUser
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

@Singleton
class AuthSessionLocalStore @Inject constructor(
    @ApplicationContext context: Context
) {

    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(DATASTORE_NAME) }
    )

    suspend fun save(authUser: AuthUser) {
        dataStore.edit { preferences ->
            preferences[KEY_UID] = authUser.uid
            preferences[KEY_EMAIL] = authUser.email
            preferences[KEY_FULL_NAME] = authUser.fullName
            preferences[KEY_PHONE] = authUser.phone
            preferences[KEY_CREATED_AT_MILLIS] = authUser.createdAtMillis ?: UNKNOWN_CREATED_AT
            preferences[KEY_ROLES] = authUser.roles.joinToString(",") { it.storageValue }
            preferences[KEY_ACTIVE_ROLE] = authUser.activeRole.storageValue
        }
    }

    suspend fun get(): AuthUser? {
        return dataStore.data
            .map { preferences ->
                val uid = preferences[KEY_UID].orEmpty()
                val email = preferences[KEY_EMAIL].orEmpty()
                if (uid.isBlank() || email.isBlank()) {
                    return@map null
                }

                val roles = preferences[KEY_ROLES]
                    .orEmpty()
                    .split(",")
                    .mapNotNull { UserRole.fromStorageValue(it) }
                    .toSet()
                    .ifEmpty { setOf(UserRole.PASSENGER) }

                val activeRole = UserRole.fromStorageValue(preferences[KEY_ACTIVE_ROLE])
                    ?.takeIf { it in roles }
                    ?: roles.firstOrNull()
                    ?: UserRole.PASSENGER

                AuthUser(
                    uid = uid,
                    email = email,
                    fullName = preferences[KEY_FULL_NAME].orEmpty(),
                    phone = preferences[KEY_PHONE].orEmpty(),
                    createdAtMillis = preferences[KEY_CREATED_AT_MILLIS]
                        ?.takeIf { it != UNKNOWN_CREATED_AT },
                    roles = roles,
                    activeRole = activeRole
                )
            }
            .firstOrNull()
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        const val DATASTORE_NAME = "auth_session.preferences_pb"
        const val UNKNOWN_CREATED_AT = -1L

        val KEY_UID = stringPreferencesKey("uid")
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_FULL_NAME = stringPreferencesKey("full_name")
        val KEY_PHONE = stringPreferencesKey("phone")
        val KEY_CREATED_AT_MILLIS = longPreferencesKey("created_at_millis")
        val KEY_ROLES = stringPreferencesKey("roles")
        val KEY_ACTIVE_ROLE = stringPreferencesKey("active_role")
    }
}
