package com.mnmyounus.yala.presentation.setup.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnmyounus.yala.domain.model.LockScope
import com.mnmyounus.yala.domain.model.LockType
import com.mnmyounus.yala.domain.usecase.SetupLockUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LockSetupUiState(
    val step: SetupStep = SetupStep.ENTER,
    val firstEntry: String = "",
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

enum class SetupStep { ENTER, CONFIRM }

@HiltViewModel
class LockSetupViewModel @Inject constructor(
    private val setupLockUseCase: SetupLockUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LockSetupUiState())
    val uiState: StateFlow<LockSetupUiState> = _uiState.asStateFlow()

    /** Call once the user finishes entering the credential the first time. */
    fun onFirstEntryComplete(value: String) {
        _uiState.value = _uiState.value.copy(step = SetupStep.CONFIRM, firstEntry = value, errorMessage = null)
    }

    /** Call once the user re-enters the credential to confirm it matches. */
    fun onConfirmEntry(
        value: String,
        packageName: String,
        label: String,
        type: LockType,
        hint: String?,
        imagePoolUris: List<String> = emptyList(),
        secretSequenceLength: Int = 0
    ) {
        val current = _uiState.value
        if (value != current.firstEntry) {
            _uiState.value = current.copy(
                step = SetupStep.ENTER,
                firstEntry = "",
                errorMessage = "That didn't match. Let's try again."
            )
            return
        }
        viewModelScope.launch {
            setupLockUseCase.setForApp(
                packageName = packageName,
                label = label,
                scope = LockScope.INDIVIDUAL,
                type = type,
                plainSecret = value,
                hint = hint,
                imagePoolUris = imagePoolUris,
                secretSequenceLength = secretSequenceLength
            )
            _uiState.value = current.copy(isSaved = true)
        }
    }

    fun reset() {
        _uiState.value = LockSetupUiState()
    }
}
