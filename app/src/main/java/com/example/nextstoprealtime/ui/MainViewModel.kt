package com.example.nextstoprealtime.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nextstoprealtime.data.BustimesRepository
import com.example.nextstoprealtime.model.Departure
import com.example.nextstoprealtime.model.Stop
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val searchQuery: String = "",
    val stops: List<Stop> = emptyList(),
    val isSearching: Boolean = false,
    val selectedStop: Stop? = null,
    val departures: List<Departure> = emptyList(),
    val isLoadingDepartures: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long? = null
)

class MainViewModel(
    private val repository: BustimesRepository = BustimesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query, error = null) }
        searchJob?.cancel()
        if (query.length < 2) {
            _uiState.update { it.copy(stops = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(350) // debounce
            _uiState.update { it.copy(isSearching = true) }
            repository.searchStops(query)
                .onSuccess { stops ->
                    _uiState.update {
                        it.copy(stops = stops, isSearching = false, error = null)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            error = e.message ?: "Search failed",
                            stops = emptyList()
                        )
                    }
                }
        }
    }

    fun selectStop(stop: Stop) {
        _uiState.update {
            it.copy(
                selectedStop = stop,
                departures = emptyList(),
                error = null
            )
        }
        loadDepartures()
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedStop = null,
                departures = emptyList(),
                error = null
            )
        }
    }

    fun loadDepartures() {
        val stop = _uiState.value.selectedStop ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDepartures = true, error = null) }
            repository.getNextDepartures(stop.atcoCode, 5)
                .onSuccess { deps ->
                    _uiState.update {
                        it.copy(
                            departures = deps,
                            isLoadingDepartures = false,
                            lastUpdated = System.currentTimeMillis(),
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingDepartures = false,
                            error = e.message ?: "Failed to load departures"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
