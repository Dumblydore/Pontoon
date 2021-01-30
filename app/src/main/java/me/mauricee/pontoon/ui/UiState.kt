package me.mauricee.pontoon.ui

import android.content.Context
import androidx.annotation.StringRes
import me.mauricee.pontoon.common.LazyLayout
import java.lang.Exception

sealed class UiState {
    open val error: UiError? = null
    object Empty : UiState()
    object Loading : UiState()
    object Refreshing : UiState()
    object Success : UiState()
    data class Failed(override val error: UiError) : UiState()

    fun lazyState(): Int = when (this) {
        Loading -> LazyLayout.LOADING
        Empty -> LazyLayout.SUCCESS
        Refreshing -> LazyLayout.SUCCESS
        Success -> LazyLayout.SUCCESS
        is Failed -> LazyLayout.ERROR
    }

    fun isRefreshing(): Boolean = this is Refreshing
}

data class UiError(@StringRes val message: Int? = null,
                   val exception: Exception? = null) {
    fun text(context: Context): String? = message?.let(context::getString) ?: exception?.message
}