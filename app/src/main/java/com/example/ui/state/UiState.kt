package com.example.ui.state

/**
 * Universal Reactive UI State representation
 */
sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>
    object Empty : UiState<Nothing>
}

/**
 * Single-fire UI Event representation for Snackbars, Dialogs and Navigation
 */
sealed interface UiEvent {
    data class ShowMessage(val message: String, val isError: Boolean = false) : UiEvent
    data class NavigateTo(val route: String) : UiEvent
}

/**
 * Helper extensions to map data lists to clean UI state
 */
fun <T> List<T>?.toUiState(): UiState<List<T>> {
    return when {
        this == null -> UiState.Loading
        this.isEmpty() -> UiState.Empty
        else -> UiState.Success(this)
    }
}
