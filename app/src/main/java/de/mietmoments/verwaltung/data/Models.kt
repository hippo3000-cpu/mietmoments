package de.mietmoments.verwaltung.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PairResponse(
    val ok: Boolean = false,
    val token: String = "",
    val message: String = ""
)

@Serializable
data class SnapshotResponse(
    val ok: Boolean = false,
    @SerialName("api_version") val apiVersion: String = "",
    @SerialName("generated_at") val generatedAt: String = "",
    val range: DateRange = DateRange(),
    val events: List<EventDto> = emptyList(),
    val customers: List<CustomerDto> = emptyList(),
    val items: List<ItemDto> = emptyList(),
    val locations: List<LocationDto> = emptyList(),
    @SerialName("customer_count") val customerCount: Int = 0,
    @SerialName("item_count") val itemCount: Int = 0,
    @SerialName("location_count") val locationCount: Int = 0,
    val message: String = ""
)

@Serializable
data class DateRange(val from: String = "", val to: String = "")

@Serializable
data class EventDto(
    @SerialName("order_id") val orderId: Int = 0,
    @SerialName("customer_id") val customerId: Int = 0,
    @SerialName("customer_no") val customerNo: String = "",
    val customer: String = "",
    val address: String = "",
    @SerialName("package_name") val packageName: String = "",
    @SerialName("background_name") val backgroundName: String = "",
    @SerialName("box_name") val boxName: String = "",
    val date: String = "",
    val source: String = "order",
    val title: String = "Veranstaltung",
    val detail: String = "",
    @SerialName("detail_extra") val detailExtra: String = ""
)

@Serializable
data class CustomerDto(
    val id: Int = 0,
    @SerialName("customer_no") val customerNo: String = "",
    val company: String = "",
    @SerialName("first_name") val firstName: String = "",
    @SerialName("last_name") val lastName: String = "",
    val phone: String = "",
    val email: String = "",
    val street: String = "",
    @SerialName("house_no") val houseNo: String = "",
    @SerialName("postal_code") val postalCode: String = "",
    val city: String = "",
    val notes: String = "",
    @SerialName("order_id") val orderId: Int = 0,
    @SerialName("has_mm") val hasMm: FlexibleBoolean = FlexibleBoolean(false),
    @SerialName("has_photo") val hasPhoto: FlexibleBoolean = FlexibleBoolean(false)
) {
    val displayName: String
        get() = company.ifBlank { listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ") }.ifBlank { "Ohne Namen" }
    val address: String
        get() = listOf(listOf(street, houseNo).filter { it.isNotBlank() }.joinToString(" "), listOf(postalCode, city).filter { it.isNotBlank() }.joinToString(" ")).filter { it.isNotBlank() }.joinToString(", ")
}

@Serializable(with = FlexibleBooleanSerializer::class)
data class FlexibleBoolean(val value: Boolean)

@Serializable
data class ItemDto(
    val id: Int = 0,
    val sku: String = "",
    val name: String = "",
    val category: String = "",
    val unit: String = "",
    @SerialName("price_type") val priceType: String = "",
    @SerialName("unit_price") val unitPrice: Double = 0.0,
    val deposit: Double = 0.0,
    val variant: String = "",
    @SerialName("storage_location") val storageLocation: String = "",
    val stock: Double? = null
)

@Serializable
data class LocationDto(
    val id: Int = 0,
    val name: String = "",
    val street: String = "",
    @SerialName("house_no") val houseNo: String = "",
    @SerialName("postal_code") val postalCode: String = "",
    val city: String = "",
    val homepage: String = "",
    @SerialName("contact_name") val contactName: String = "",
    @SerialName("contact_phone") val contactPhone: String = "",
    @SerialName("contact_email") val contactEmail: String = "",
    @SerialName("wifi_ssid") val wifiSsid: String = "",
    @SerialName("wifi_password") val wifiPassword: String = "",
    @SerialName("wifi_note") val wifiNote: String = "",
    @SerialName("access_info") val accessInfo: String = "",
    val notes: String = "",
    val label: String = "",
    val venue: String = "",
    val address: String = ""
) {
    val displayAddress: String
        get() = address.ifBlank {
            listOf(listOf(street, houseNo).filter { it.isNotBlank() }.joinToString(" "), listOf(postalCode, city).filter { it.isNotBlank() }.joinToString(" ")).filter { it.isNotBlank() }.joinToString(", ")
        }
}

@Serializable
data class CustomerDetailResponse(
    val ok: Boolean = false,
    val customer: CustomerDto = CustomerDto(),
    val orders: List<OrderDto> = emptyList(),
    val selected: SelectedOrder = SelectedOrder(),
    val warnings: List<String> = emptyList(),
    val message: String = ""
)

@Serializable
data class SelectedOrder(
    val order: OrderDto? = null,
    val rental: RentalBookingDto? = null,
    val photobooth: PhotoboothDto? = null,
    val location: LocationDto? = null
)

@Serializable
data class OrderDto(
    val id: Int = 0,
    @SerialName("order_no") val orderNo: String = "",
    @SerialName("order_type") val orderType: String = "",
    @SerialName("event_date") val eventDate: String = "",
    @SerialName("event_street") val eventStreet: String = "",
    @SerialName("event_house_no") val eventHouseNo: String = "",
    @SerialName("event_postal_code") val eventPostalCode: String = "",
    @SerialName("event_city") val eventCity: String = "",
    val venue: String = "",
    @SerialName("location_id") val locationId: Int? = null,
    val status: String = "",
    val notes: String = ""
) {
    val eventAddress: String
        get() = listOf(listOf(eventStreet, eventHouseNo).filter { it.isNotBlank() }.joinToString(" "), listOf(eventPostalCode, eventCity).filter { it.isNotBlank() }.joinToString(" ")).filter { it.isNotBlank() }.joinToString(", ")
}

@Serializable
data class RentalBookingDto(
    val id: Int = 0,
    @SerialName("pickup_date") val pickupDate: String = "",
    @SerialName("event_date") val eventDate: String = "",
    @SerialName("return_date") val returnDate: String = "",
    @SerialName("fixed_total_price") val fixedTotalPrice: Double? = null,
    val deposit: Double = 0.0,
    val notes: String = "",
    val positions: List<RentalPositionDto> = emptyList(),
    @SerialName("rental_period_label") val rentalPeriodLabel: String = "",
    @SerialName("rental_period_days") val rentalPeriodDays: Int = 0
) {
    val calculatedTotal: Double get() = positions.sumOf { it.lineTotal }
    val total: Double get() = fixedTotalPrice ?: calculatedTotal
}

@Serializable
data class RentalPositionDto(
    val id: Int = 0,
    @SerialName("item_name") val itemName: String = "",
    @SerialName("variant_snapshot") val variantSnapshot: String = "",
    val quantity: Double = 0.0,
    @SerialName("unit_price") val unitPrice: Double = 0.0,
    @SerialName("line_total") val lineTotal: Double = 0.0,
    val notes: String = ""
)

@Serializable
data class PhotoboothDto(
    val id: Int = 0,
    @SerialName("package_name") val packageName: String = "",
    @SerialName("package_price") val packagePrice: Double = 0.0,
    @SerialName("background_name") val backgroundName: String = "",
    @SerialName("box_name") val boxName: String = "",
    @SerialName("set_name") val setName: String = "",
    @SerialName("layout_name") val layoutName: String = "",
    @SerialName("start_date") val startDate: String = "",
    @SerialName("end_date") val endDate: String = "",
    @SerialName("total_price") val totalPrice: Double = 0.0,
    @SerialName("internet_option") val internetOption: FlexibleBoolean = FlexibleBoolean(false),
    @SerialName("second_printer") val secondPrinter: FlexibleBoolean = FlexibleBoolean(false),
    @SerialName("extra_300_prints") val extraPrints: FlexibleBoolean = FlexibleBoolean(false),
    @SerialName("dsgvo_option") val dsgvo: FlexibleBoolean = FlexibleBoolean(false)
)
