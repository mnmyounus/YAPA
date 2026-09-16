package com.mnmyounus.yala.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnmyounus.yala.domain.repository.LockRepository
import com.mnmyounus.yala.presentation.theme.AppThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    lockRepository: LockRepository
) : ViewModel() {

    // Null until the first read from encrypted storage completes, so the UI can
    // avoid flashing the wrong start screen on cold start.
    private val _isOnboarded = MutableStateFlow<Boolean?>(null)
    val isOnboarded: StateFlow<Boolean?> = _isOnboarded.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    init {
        viewModelScope.launch {
            lockRepository.isOnboarded().collect { _isOnboarded.value = it }
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }
}
