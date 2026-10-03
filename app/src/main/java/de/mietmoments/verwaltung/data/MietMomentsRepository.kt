package de.mietmoments.verwaltung.data

import android.content.Context
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class MietMomentsRepository(context: Context) {
    val settingsStore = AppSettingsStore(context)
    private val cache = CacheStore(context)
    private val api = MobileApi()

    fun cachedSnapshot(): SnapshotResponse? = cache.readSnapshot()?.let { raw ->
        runCatching { api.json.decodeFromString<SnapshotResponse>(raw) }.getOrNull()
    }

    fun cachedCustomer(customerId: Int, orderId: Int): CustomerDetailResponse? = cache.readCustomer(customerId, orderId)?.let { raw ->
        runCatching { api.json.decodeFromString<CustomerDetailResponse>(raw) }.getOrNull()
    }

    suspend fun test(settings: AppSettings): Boolean = api.health(settings)

    suspend fun sync(settings: AppSettings, weekStart: LocalDate): SnapshotResponse {
        val from = weekStart.toString()
        val to = weekStart.plusDays(6).toString()
        val (data, raw) = api.snapshot(settings, from, to)
        cache.writeSnapshot(raw)
        return data
    }

    suspend fun customer(settings: AppSettings, customerId: Int, orderId: Int): CustomerDetailResponse {
        val (data, raw) = api.customer(settings, customerId, orderId)
        val resolvedOrder = data.selected.order?.id ?: orderId
        cache.writeCustomer(customerId, resolvedOrder, raw)
        return data
    }

    companion object {
        fun currentWeekStart(today: LocalDate = LocalDate.now()): LocalDate =
            today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
}
