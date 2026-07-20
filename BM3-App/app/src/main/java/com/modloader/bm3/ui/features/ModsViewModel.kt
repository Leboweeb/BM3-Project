package com.modloader.bm3.ui.features

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modloader.bm3.domain.model.ModModel
import com.modloader.bm3.domain.usecases.GetInstalledModsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ModsScreenState(
    val mods :List<ModModel>,
    val searchBarQuery : String,
    val isLoading : Boolean,
    val errors : List<Exception>,
    // NEEDS TO BE ZERO INDEXED
    val currentPage : Int
)

@HiltViewModel
class ModsViewModel @Inject constructor(
    private val installedModsUseCase: GetInstalledModsUseCase
) : ViewModel() {


    private val _modsScreenState = MutableStateFlow(
        ModsScreenState(
            listOf(),
            "",
            true,
            listOf(),
            0
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

    fun getMods() {
        _modsScreenState.value.mods.toList()
    }
    fun refreshMods() {
        viewModelScope.launch {
            _modsScreenState.update {
                it.copy(
                    mods = installedModsUseCase(page = it.currentPage).toList().flatten(),
                    currentPage = it.currentPage,
                    searchBarQuery = it.searchBarQuery,
                    isLoading = false,
                    errors = it.errors
                )
            }
        }
    }

    init {
        refreshMods()
    }
}