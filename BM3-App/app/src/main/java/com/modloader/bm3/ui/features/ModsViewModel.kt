package com.modloader.bm3.ui.features

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modloader.bm3.domain.model.ModModel
import com.modloader.bm3.domain.usecases.GetModsUseCase
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


enum class TARGETATTRIBUTE  {
    MODS, INSTALLED_MODS, DISABLED_MODS
}
data class ModsScreenState(
    val mods: List<ModModel>,
    val installedMods: List<ModModel>,
    val disabledMods: List<ModModel>,
    val searchBarQuery: String,
    val isLoading: Boolean,
    val errors: List<Exception>,
    // NEEDS TO BE ZERO INDEXED
    val currentPage: Int
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
            errors = listOf(),
            currentPage = 0
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
                updateMods ( { it.title.lowercase().contains(currentQuery.lowercase()) } , targetattribute = TARGETATTRIBUTE.MODS)
            }
        }
    }

    private fun updateState(newStateFun: (ModsScreenState) -> ModsScreenState) {
        viewModelScope.launch {
            try {
                _modsScreenState.update(function = newStateFun)
            } catch (e: CancellationException) {
                Log.d("StateDebug", "Coroutine was canceled! Details below :\n ${e.message}")
            }
        }
    }

    fun updateMods(filter: (ModModel) -> Boolean, targetattribute: TARGETATTRIBUTE) {
        viewModelScope.launch {
            try {
                getModsUseCase(
                    page = _modsScreenState.value.currentPage, filter = filter
                ).collect {
                _modsScreenState.update { prevState ->
                    // this is stupid yes, but I couldn't find a way to
                    // elegantly represent this with a lambda
                    val newState = when(targetattribute) {
                        TARGETATTRIBUTE.MODS -> prevState.copy(
                            mods = it,
                            isLoading = false,
                        )

                        TARGETATTRIBUTE.INSTALLED_MODS -> prevState.copy(
                            installedMods = it,
                            isLoading = false,
                        )

                        TARGETATTRIBUTE.DISABLED_MODS -> prevState.copy(
                            installedMods = it,
                            isLoading = false,
                        )

                    }
                    newState
                }
                }
            } catch (e: CancellationException) {
                Log.d("StateDebug", "Coroutine was canceled! Details below :\n ${e.message}")
            }
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
        updateMods ( { it.isInstalled } , TARGETATTRIBUTE.INSTALLED_MODS)
        updateMods ( { it.isDisabled} , TARGETATTRIBUTE.INSTALLED_MODS)
    }
}