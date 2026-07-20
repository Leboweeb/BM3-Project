package com.modloader.bm3.ui.features

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modloader.bm3.domain.model.ModModel
import com.modloader.bm3.domain.usecases.GetInstalledModsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ModsScreenState(
    val mods: List<ModModel>,
    val searchBarQuery: String,
    val isLoading: Boolean,
    val errors: List<Exception>,
    // NEEDS TO BE ZERO INDEXED
    val currentPage: Int
)

@HiltViewModel
class ModsViewModel @Inject constructor(
    private val installedModsUseCase: GetInstalledModsUseCase
) : ViewModel() {


    private val _modsScreenState = MutableStateFlow(
        ModsScreenState(
            listOf(), "", true, listOf(), 0
        )
    )

    val modsScreenState = _modsScreenState.asStateFlow()

    fun markUpdating() {
        _modsScreenState.update {
            it.copy(
                mods = it.mods,
                currentPage = it.currentPage,
                searchBarQuery = it.searchBarQuery,
                isLoading = true,
                errors = it.errors
            )
        }
    }

    fun getMods(): List<ModModel> {
        return _modsScreenState.value.mods.toList()
    }

    fun refreshMods() {
        viewModelScope.launch {
            try {
                Log.d("StateDebug", "Refreshing viewmodel mods...")
                val newMods =
                    installedModsUseCase(page = _modsScreenState.value.currentPage).first().toList()
                _modsScreenState.update { prevState ->
                    val newState = prevState.copy(
                        mods = newMods,
                        isLoading = false,
                    )
                    // Debug checks
                    Log.d("StateDebug", "Are objects equal? ${prevState == newState}")
                    Log.d("StateDebug", "Old: $prevState")
                    Log.d("StateDebug", "New: $newState")
                    newState
                }
            } catch (e: CancellationException) {
                Log.d("StateDebug", "Coroutine was canceled! Details below :\n ${e.message}")
            }
        }
    }

    init {
        refreshMods()
    }
}