package com.modloader.bm3.ui.features

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modloader.bm3.domain.usecases.GetModsUseCase
import com.modloader.bm3.ui.mapping.toUIModels
import com.modloader.bm3.ui.model.Mod
import com.modloader.bm3.utils.DEBOUNCEMILLIS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


data class ModsScreenState(
    val mods: List<Mod>,
    val installedMods: List<Mod>,
    val disabledMods: List<Mod>,
    val searchBarQuery: String,
    val isLoading: Boolean,
    val isBottomSheetShown: Boolean,
    val errors: List<Exception>,
    // NEEDS TO BE ZERO INDEXED
    val currentPage: Int,
    val currentCardSelected: Mod?
)

@HiltViewModel
class ModsViewModel @Inject constructor(
    private val getModsUseCase: GetModsUseCase
) : ViewModel() {


    private var currentSearchJob: Job? = null
    private val _modsScreenState = MutableStateFlow(
        ModsScreenState(
            mods = listOf(),
            installedMods = listOf(),
            disabledMods = listOf(),
            searchBarQuery = "",
            isLoading = false,
            isBottomSheetShown = false,
            errors = listOf(),
            currentPage = 0,
            currentCardSelected = null,

            )
    )

    val modsScreenState = _modsScreenState.asStateFlow()


    fun onSearchTextChanged(query: String) {
        // ensure latest state is updated
        viewModelScope.launch {
            _modsScreenState.update {
                it.copy(
                    searchBarQuery = query
                )
            }
        }
        // and that previous search job is canceled if any
        currentSearchJob?.cancel()
        val currentQuery = _modsScreenState.value.searchBarQuery
        currentSearchJob = viewModelScope.launch {
            // debounce 500 millis to not spam DB reads
            delay(DEBOUNCEMILLIS.milliseconds)
            if (currentQuery.isNotBlank()) {
                findMods(currentQuery)
            }
        }
    }

    private fun updateState(stateFun: (ModsScreenState) -> ModsScreenState) {
        viewModelScope.launch {
            try {
                _modsScreenState.update(stateFun)

            } catch (e: CancellationException) {
                Log.d("StateDebug", "Coroutine was canceled! Details below :\n ${e.message}")
            }
        }
    }

    private fun setActiveMod(mod: Mod) {
        updateState {
            it.copy(
                currentCardSelected = mod
            )
        }
    }

    // this is stupid yes, but I couldn't find a way to
    // elegantly represent this with a lambda
    fun findMods(query: String) {
        viewModelScope.launch {
            try {
                getModsUseCase(
                    page = _modsScreenState.value.currentPage,
                    filter = { it.title.lowercase().contains(query.lowercase()) }).collect {
                    _modsScreenState.update { prevState ->
                        val newState = prevState.copy(
                            mods = it.toUIModels(),
                            isLoading = false,
                        )
                        newState
                    }
                }

            } catch (e: CancellationException) {
                Log.d("StateDebug", "Coroutine was canceled! Details below :\n ${e.message}")
            }
        }
    }

    fun updateMods() {
        viewModelScope.launch {
            try {
                getModsUseCase(
                    page = _modsScreenState.value.currentPage,
                    filter = { it.isInstalled or it.isDisabled }).collect { modModels ->
                    _modsScreenState.update { prevState ->

                        val installedMods = modModels.filter { it.isInstalled }
                        val disabledMods = modModels.filter { it.isDisabled }
                        prevState.copy(
                            installedMods = installedMods.toUIModels(),
                            disabledMods = disabledMods.toUIModels()
                        )
                    }
                }
            } catch (e: CancellationException) {
                Log.d("StateDebug", "Coroutine was canceled! Details below :\n ${e.message}")
            }
        }
    };

    fun onDismissSheet() {
        updateState {
            it.copy(
                currentCardSelected = null,
                isBottomSheetShown = false
            )
        }
    }

    fun onCardSelected(mod: Mod) {
        setActiveMod(mod)
        updateState {
            it.copy(
                isBottomSheetShown = true
            )
        }
    }


    fun resetMods() {
        updateState {
            it.copy(
                mods = listOf()
            )
        }
    }


    init {
        updateMods()
    }
}