package com.mnmyounus.yala.presentation.applist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnmyounus.yala.domain.model.InstalledApp
import com.mnmyounus.yala.domain.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppListUiState(
    val apps: List<InstalledApp> = emptyList(),
    val showSystemApps: Boolean = false,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _showSystemApps = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _allApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val _isLoading = MutableStateFlow(true)

    val uiState: StateFlow<AppListUiState> = combine(
        _allApps, _showSystemApps, _searchQuery, _isLoading
    ) { apps, showSystem, query, loading ->
        AppListUiState(
            apps = apps
                .filter { showSystem || !it.isSystemApp }
                .filter { query.isBlank() || it.label.contains(query, ignoreCase = true) },
            showSystemApps = showSystem,
            searchQuery = query,
            isLoading = loading
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppListUiState())

    init {
        refresh()
        viewModelScope.launch {
            appRepository.observeLockedApps().collect { locked ->
                val lockedPackages = locked.map { it.packageName }.toSet()
                _allApps.value = _allApps.value.map { it.copy(isLocked = it.packageName in lockedPackages) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _allApps.value = appRepository.getInstalledApps(includeSystemApps = true)
            _isLoading.value = false
        }
    }

    fun toggleShowSystemApps() {
        _showSystemApps.value = !_showSystemApps.value
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun unlockApp(packageName: String) {
        viewModelScope.launch { appRepository.unlockApp(packageName) }
    }
}
