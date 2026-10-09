package de.mietmoments.verwaltung.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.mietmoments.verwaltung.data.AppSettings
import de.mietmoments.verwaltung.data.CustomerDetailResponse
import de.mietmoments.verwaltung.data.CustomerDto
import de.mietmoments.verwaltung.data.ItemDto
import de.mietmoments.verwaltung.data.LocationDto
import de.mietmoments.verwaltung.data.MietMomentsRepository
import de.mietmoments.verwaltung.data.OrderDto
import de.mietmoments.verwaltung.data.PhotoboothDto
import de.mietmoments.verwaltung.data.RentalBookingDto
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
    val customerLoading: Boolean = false,
    val saving: Boolean = false
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

    fun saveSetup(serverUrl: String, pairCode: String, momo: Boolean, animations: Boolean, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val normalizedServer = serverUrl.trim().let { if (it.endsWith('/')) it else "$it/" }
            val normalizedCode = pairCode.trim().uppercase().replace(Regex("[^A-Z0-9]"), "")
            if (!normalizedServer.startsWith("https://")) {
                onResult(false, "Bitte eine HTTPS-Serveradresse eintragen.")
                return@launch
            }
            if (normalizedCode.length != 8) {
                onResult(false, "Bitte den 8-stelligen Kopplungscode aus der Verwaltung eingeben.")
                return@launch
            }

            _state.update { it.copy(syncing = true, error = null) }
            val result = runCatching {
                val token = repository.pair(normalizedServer, normalizedCode)
                val candidate = AppSettings(normalizedServer, token, momo, animations)
                if (!repository.test(candidate)) error("Verbindungstest fehlgeschlagen.")
                candidate
            }

            result.onSuccess { candidate ->
                repository.settingsStore.save(
                    candidate.serverUrl,
                    candidate.token,
                    candidate.momoEnabled,
                    candidate.animationsEnabled
                )
                _state.update {
                    it.copy(
                        settings = candidate,
                        configured = true,
                        syncing = false,
                        online = true,
                        error = null
                    )
                }
                sync()
                onResult(true, "Gekoppelt. Momo kennt jetzt den Weg.")
            }.onFailure { error ->
                _state.update { it.copy(syncing = false, online = false, error = error.message) }
                onResult(false, error.message ?: "Kopplung fehlgeschlagen.")
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


    fun saveCustomerDetail(
        customer: CustomerDto,
        order: OrderDto,
        rental: RentalBookingDto?,
        photobooth: PhotoboothDto?,
        onResult: (Boolean, String) -> Unit
    ) {
        val current = _state.value
        if (current.saving) return
        val customerId = customer.id
        val orderId = order.id
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            runCatching {
                val saved = repository.saveBooking(
                    settings = _state.value.settings,
                    customerId = customerId,
                    orderId = orderId,
                    customer = customer,
                    order = order,
                    rental = rental,
                    photobooth = photobooth
                )
                val savedCustomerId = saved.customerId.takeIf { it > 0 } ?: customerId
                val savedOrderId = saved.orderId.takeIf { it > 0 } ?: orderId
                val freshDetail = repository.customer(_state.value.settings, savedCustomerId, savedOrderId)
                val freshSnapshot = repository.sync(_state.value.settings, _state.value.weekStart)
                Triple(saved, freshDetail, freshSnapshot)
            }.onSuccess { (saved, detail, snapshot) ->
                _state.update {
                    it.copy(
                        customerDetail = detail,
                        snapshot = snapshot,
                        saving = false,
                        online = true,
                        error = null
                    )
                }
                onResult(true, saved.message.ifBlank { "Änderungen gespeichert." })
            }.onFailure { error ->
                val message = error.message ?: "Änderungen konnten nicht gespeichert werden."
                _state.update { it.copy(saving = false, online = false, error = message) }
                onResult(false, message)
            }
        }
    }

    fun saveLocation(location: LocationDto, onResult: (Boolean, String) -> Unit) {
        if (_state.value.saving) return
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            runCatching {
                val saved = repository.saveLocation(_state.value.settings, location)
                val snapshot = repository.sync(_state.value.settings, _state.value.weekStart)
                saved to snapshot
            }.onSuccess { (saved, snapshot) ->
                _state.update { it.copy(snapshot = snapshot, saving = false, online = true, error = null) }
                onResult(true, saved.message.ifBlank { "Location gespeichert." })
            }.onFailure { error ->
                val message = error.message ?: "Location konnte nicht gespeichert werden."
                _state.update { it.copy(saving = false, online = false, error = message) }
                onResult(false, message)
            }
        }
    }

    fun saveItem(item: ItemDto, onResult: (Boolean, String) -> Unit) {
        if (_state.value.saving) return
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            runCatching {
                val saved = repository.saveItem(_state.value.settings, item)
                val snapshot = repository.sync(_state.value.settings, _state.value.weekStart)
                saved to snapshot
            }.onSuccess { (saved, snapshot) ->
                _state.update { it.copy(snapshot = snapshot, saving = false, online = true, error = null) }
                onResult(true, saved.message.ifBlank { "Artikel gespeichert." })
            }.onFailure { error ->
                val message = error.message ?: "Artikel konnte nicht gespeichert werden."
                _state.update { it.copy(saving = false, online = false, error = message) }
                onResult(false, message)
            }
        }
    }

    fun clearCustomer() = _state.update { it.copy(customerDetail = null, customerLoading = false) }
}
