package de.mietmoments.verwaltung.data

import android.content.Context
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.UUID

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

    suspend fun pair(serverUrl: String, code: String): String = api.pair(serverUrl, code)

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

    suspend fun saveBooking(
        settings: AppSettings,
        customerId: Int,
        orderId: Int,
        customer: CustomerDto,
        order: OrderDto,
        rental: RentalBookingDto?,
        photobooth: PhotoboothDto?
    ): SaveResponse = api.saveBooking(
        settings,
        SaveBookingRequest(
            operationUuid = UUID.randomUUID().toString(),
            customerId = customerId,
            orderId = orderId,
            useMietMoments = rental != null,
            usePhotobooth = photobooth != null,
            customer = customer,
            order = order,
            rental = rental,
            photobooth = photobooth
        )
    )

    suspend fun saveLocation(settings: AppSettings, location: LocationDto): SaveResponse =
        api.saveLocation(
            settings,
            SaveLocationRequest(
                operationUuid = UUID.randomUUID().toString(),
                locationId = location.id,
                location = location
            )
        )

    suspend fun saveItem(settings: AppSettings, item: ItemDto): SaveResponse =
        api.saveItem(
            settings,
            SaveItemRequest(
                operationUuid = UUID.randomUUID().toString(),
                itemId = item.id,
                item = item
            )
        )

    companion object {
        fun currentWeekStart(today: LocalDate = LocalDate.now()): LocalDate =
            today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
}
