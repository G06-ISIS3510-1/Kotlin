package com.wheels.app.features.favoriteDrivers.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.wheels.app.features.favoriteDrivers.analytics.domain.model.FavoriteDriverAnalyticsSummary
import com.wheels.app.features.favoriteDrivers.analytics.domain.repository.FavoriteDriverAnalyticsRepository
import com.wheels.app.features.favoriteDrivers.domain.model.FavoriteDriver
import com.wheels.app.features.favoriteDrivers.domain.repository.FavoriteDriverRepository
import com.wheels.app.features.favoriteDrivers.sync.FavoriteDriverSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class FavoriteDriversViewModel @Inject constructor(
    private val repository: FavoriteDriverRepository,
    private val analyticsRepository: FavoriteDriverAnalyticsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteDriversUiState())
    val uiState: StateFlow<FavoriteDriversUiState> = _uiState.asStateFlow()

    fun observeFavorites() {
        viewModelScope.launch {
            repository.observeFavoriteDrivers()
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Favorite drivers could not be loaded."
                        )
                    }
                }
                .collect { drivers ->
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                drivers = drivers,
                                errorMessage = null
                            )
                        }
                    }
                }
        }
    }

    fun loadMostFavoritedDrivers() {
        viewModelScope.launch {
            val ranking = withContext(Dispatchers.IO) {
                analyticsRepository.getMostFavoritedDrivers()
            }
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(mostFavoritedDrivers = ranking) }
            }
        }
    }

    fun observeDriver(driverId: String) {
        viewModelScope.launch {
            repository.observeFavoriteDriver(driverId)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Favorite driver could not be loaded."
                        )
                    }
                }
                .collect { driver ->
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                selectedDriver = driver,
                                errorMessage = null
                            )
                        }
                    }
                }
        }
    }

    fun favoriteDriver(driver: FavoriteDriver) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.favoriteDriver(driver)
                    repository.syncPendingFavorites()
                }
                enqueuePendingSync()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "Favorite driver could not be saved.")
                }
            }
        }
    }

    fun unfavoriteDriver(driverId: String) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.unfavoriteDriver(driverId)
                    repository.syncPendingFavorites()
                }
                enqueuePendingSync()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "Favorite driver could not be removed.")
                }
            }
        }
    }

    fun addDemoFavorite() {
        favoriteDriver(
            FavoriteDriver(
                driverId = "demo-driver-1",
                driverName = "Carlos Mendez",
                rating = 4.8,
                trustScore = 96.0,
                completedRides = 128,
                profileImageUrl = null,
                savedAt = System.currentTimeMillis(),
                pendingSync = true
            )
        )
    }

    private fun enqueuePendingSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<FavoriteDriverSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            FavoriteDriverSyncWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}

data class FavoriteDriversUiState(
    val isLoading: Boolean = true,
    val drivers: List<FavoriteDriver> = emptyList(),
    val selectedDriver: FavoriteDriver? = null,
    val mostFavoritedDrivers: List<FavoriteDriverAnalyticsSummary> = emptyList(),
    val errorMessage: String? = null
)
