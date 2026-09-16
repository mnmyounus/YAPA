package com.mnmyounus.yala.presentation.lockscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnmyounus.yala.domain.model.LockCredential
import com.mnmyounus.yala.domain.repository.LockRepository
import com.mnmyounus.yala.domain.usecase.VerifyLockUseCase
import com.mnmyounus.yala.domain.usecase.VerifyRecoveryKeyUseCase
import com.mnmyounus.yala.domain.usecase.VerifyResult
import com.mnmyounus.yala.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LockScreenEvent {
    data object Unlocked : LockScreenEvent()
    data object WrongAttempt : LockScreenEvent()
    data object RecoveredViaKey : LockScreenEvent()
}

data class LockScreenUiState(
    val credential: LockCredential? = null,
    val failedAttempts: Int = 0,
    val showRecoveryOption: Boolean = false,
    val lastEvent: LockScreenEvent? = null
)

@HiltViewModel
class LockScreenViewModel @Inject constructor(
    private val lockRepository: LockRepository,
    private val verifyLockUseCase: VerifyLockUseCase,
    private val verifyRecoveryKeyUseCase: VerifyRecoveryKeyUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LockScreenUiState())
    val uiState: StateFlow<LockScreenUiState> = _uiState.asStateFlow()

    fun loadCredential(packageName: String) {
        viewModelScope.launch {
            val credential = lockRepository.getCredentialFor(packageName)
            _uiState.value = _uiState.value.copy(credential = credential)
        }
    }

    /** Returns true on success so the caller can trigger a "success" intruder capture + dismiss the overlay. */
    fun attemptUnlock(packageName: String, plainAttempt: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            when (verifyLockUseCase(packageName, plainAttempt)) {
                is VerifyResult.Success -> {
                    _uiState.value = _uiState.value.copy(failedAttempts = 0, lastEvent = LockScreenEvent.Unlocked)
                    onResult(true)
                }
                is VerifyResult.Failure, is VerifyResult.NoCredentialConfigured -> {
                    val attempts = _uiState.value.failedAttempts + 1
                    _uiState.value = _uiState.value.copy(
                        failedAttempts = attempts,
                        showRecoveryOption = attempts >= Constants.MAX_FAILED_ATTEMPTS_BEFORE_RECOVERY_HINT,
                        lastEvent = LockScreenEvent.WrongAttempt
                    )
                    onResult(false)
                }
            }
        }
    }

    fun attemptRecoveryKey(plainKey: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = verifyRecoveryKeyUseCase(plainKey)
            if (ok) _uiState.value = _uiState.value.copy(lastEvent = LockScreenEvent.RecoveredViaKey)
            onResult(ok)
        }
    }
}
