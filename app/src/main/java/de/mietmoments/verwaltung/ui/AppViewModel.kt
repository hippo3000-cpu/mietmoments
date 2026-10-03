package de.mietmoments.verwaltung.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.mietmoments.verwaltung.data.AppSettings
import de.mietmoments.verwaltung.data.CustomerDetailResponse
import de.mietmoments.verwaltung.data.MietMomentsRepository
import de.mietmoments.verwaltung.data.SnapshotResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AppUiState(
    val settings: AppSettings = AppSettings(),
    val configured: Boolean = false,
    val loading: Boolean = true,
    val syncing: Boolean = false,
    val online: Boolean = true,
    val error: String? = null,
    val snapshot: SnapshotResponse? = null,
    val weekStart: LocalDate = MietMomentsRepository.currentWeekStart(),
    val customerDetail: CustomerDetailResponse? = null,
    val customerLoading: Boolean = false
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MietMomentsRepository(application)
    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init {
        val settings = repository.settingsStore.load()
        val cached = repository.cachedSnapshot()
        _state.update { it.copy(settings = settings, configured = settings.configured, loading = false, snapshot = cached) }
        if (settings.configured) sync()
    }

    fun saveSetup(serverUrl: String, token: String, momo: Boolean, animations: Boolean, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val candidate = AppSettings(serverUrl.trim().let { if (it.endsWith('/')) it else "$it/" }, token.trim(), momo, animations)
            if (!candidate.serverUrl.startsWith("https://") || candidate.token.isBlank()) {
                onResult(false, "Bitte HTTPS-Server und App-Schlüssel eintragen.")
                return@launch
            }
            _state.update { it.copy(syncing = true, error = null) }
            val result = runCatching { repository.test(candidate) }
            if (result.getOrDefault(false)) {
                repository.settingsStore.save(candidate.serverUrl, candidate.token, candidate.momoEnabled, candidate.animationsEnabled)
                _state.update { it.copy(settings = candidate, configured = true, syncing = false, online = true) }
                sync()
                onResult(true, "Verbindung steht.")
            } else {
                _state.update { it.copy(syncing = false, online = false, error = result.exceptionOrNull()?.message) }
                onResult(false, result.exceptionOrNull()?.message ?: "Verbindung fehlgeschlagen.")
            }
        }
    }

    fun updatePreferences(momo: Boolean, animations: Boolean) {
        val s = _state.value.settings.copy(momoEnabled = momo, animationsEnabled = animations)
        repository.settingsStore.save(s.serverUrl, s.token, momo, animations)
        _state.update { it.copy(settings = s) }
    }

    fun sync() {
        val current = _state.value
        if (!current.configured || current.syncing) return
        viewModelScope.launch {
            _state.update { it.copy(syncing = true, error = null) }
            runCatching { repository.sync(_state.value.settings, _state.value.weekStart) }
                .onSuccess { data -> _state.update { it.copy(snapshot = data, syncing = false, online = true, error = null) } }
                .onFailure { error -> _state.update { it.copy(syncing = false, online = false, error = error.message ?: "Synchronisierung fehlgeschlagen.") } }
        }
    }

    fun moveWeek(days: Long) {
        _state.update { it.copy(weekStart = it.weekStart.plusDays(days)) }
        sync()
    }

    fun currentWeek() {
        _state.update { it.copy(weekStart = MietMomentsRepository.currentWeekStart()) }
        sync()
    }

    fun openCustomer(customerId: Int, orderId: Int = 0) {
        val settings = _state.value.settings
        val cached = repository.cachedCustomer(customerId, orderId)
        _state.update { it.copy(customerDetail = cached, customerLoading = true, error = null) }
        viewModelScope.launch {
            runCatching { repository.customer(settings, customerId, orderId) }
                .onSuccess { detail -> _state.update { it.copy(customerDetail = detail, customerLoading = false, online = true) } }
                .onFailure { error -> _state.update { it.copy(customerLoading = false, online = false, error = error.message ?: "Kundendaten konnten nicht geladen werden.") } }
        }
    }

    fun clearCustomer() = _state.update { it.copy(customerDetail = null, customerLoading = false) }
}
