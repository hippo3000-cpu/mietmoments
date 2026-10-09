package de.mietmoments.verwaltung.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.mietmoments.verwaltung.data.CustomerDetailResponse
import de.mietmoments.verwaltung.data.FlexibleBoolean
import de.mietmoments.verwaltung.data.ItemDto
import de.mietmoments.verwaltung.data.OrderDto
import de.mietmoments.verwaltung.data.PhotoboothDto
import de.mietmoments.verwaltung.data.RentalBookingDto
import de.mietmoments.verwaltung.data.RentalPositionDto
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

private data class EditablePosition(
    val key: String,
    val rentalItemId: Int?,
    var itemName: String,
    var variant: String,
    var quantity: String,
    var unitPrice: String,
    var unitDeposit: String,
    var notes: String
)

@Composable
fun CustomerEditScreen(
    detail: CustomerDetailResponse?,
    catalogItems: List<ItemDto>,
    saving: Boolean,
    onCancel: () -> Unit,
    onSave: (
        de.mietmoments.verwaltung.data.CustomerDto,
        OrderDto,
        RentalBookingDto?,
        PhotoboothDto?,
        (Boolean, String) -> Unit
    ) -> Unit
) {
    if (detail == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator()
            Text("Kundendaten werden geladen …")
        }
        return
    }

    val originalCustomer = detail.customer
    val originalOrder = detail.selected.order ?: detail.orders.firstOrNull() ?: OrderDto()
    val originalRental = detail.selected.rental
    val originalPhoto = detail.selected.photobooth

    var company by remember(detail) { mutableStateOf(originalCustomer.company) }
    var firstName by remember(detail) { mutableStateOf(originalCustomer.firstName) }
    var lastName by remember(detail) { mutableStateOf(originalCustomer.lastName) }
    var phone by remember(detail) { mutableStateOf(originalCustomer.phone) }
    var email by remember(detail) { mutableStateOf(originalCustomer.email) }
    var street by remember(detail) { mutableStateOf(originalCustomer.street) }
    var houseNo by remember(detail) { mutableStateOf(originalCustomer.houseNo) }
    var postalCode by remember(detail) { mutableStateOf(originalCustomer.postalCode) }
    var city by remember(detail) { mutableStateOf(originalCustomer.city) }
    var customerNotes by remember(detail) { mutableStateOf(originalCustomer.notes) }

    var eventDate by remember(detail) { mutableStateOf(originalOrder.eventDate) }
    var eventStreet by remember(detail) { mutableStateOf(originalOrder.eventStreet) }
    var eventHouseNo by remember(detail) { mutableStateOf(originalOrder.eventHouseNo) }
    var eventPostalCode by remember(detail) { mutableStateOf(originalOrder.eventPostalCode) }
    var eventCity by remember(detail) { mutableStateOf(originalOrder.eventCity) }
    var venue by remember(detail) { mutableStateOf(originalOrder.venue) }
    var orderNotes by remember(detail) { mutableStateOf(originalOrder.notes) }

    var pickupDate by remember(detail) { mutableStateOf(originalRental?.pickupDate.orEmpty()) }
    var rentalEventDate by remember(detail) { mutableStateOf(originalRental?.eventDate.orEmpty()) }
    var returnDate by remember(detail) { mutableStateOf(originalRental?.returnDate.orEmpty()) }
    var rentalNotes by remember(detail) { mutableStateOf(originalRental?.notes.orEmpty()) }

    var packageName by remember(detail) { mutableStateOf(originalPhoto?.packageName.orEmpty()) }
    var backgroundName by remember(detail) { mutableStateOf(originalPhoto?.backgroundName.orEmpty()) }
    var boxName by remember(detail) { mutableStateOf(originalPhoto?.boxName.orEmpty()) }
    var setName by remember(detail) { mutableStateOf(originalPhoto?.setName.orEmpty()) }
    var layoutName by remember(detail) { mutableStateOf(originalPhoto?.layoutName.orEmpty()) }
    var photoStartDate by remember(detail) { mutableStateOf(originalPhoto?.startDate.orEmpty()) }
    var photoEndDate by remember(detail) { mutableStateOf(originalPhoto?.endDate.orEmpty()) }
    var distanceKm by remember(detail) { mutableStateOf(formatNumber(originalPhoto?.distanceKm ?: 0.0)) }
    var distanceFee by remember(detail) { mutableStateOf(formatNumber(originalPhoto?.distanceFee ?: 0.0)) }
    var supportHours by remember(detail) { mutableStateOf(formatNumber(originalPhoto?.supportHours ?: 0.0)) }
    var internet by remember(detail) { mutableStateOf(originalPhoto?.internetOption?.value ?: false) }
    var secondPrinter by remember(detail) { mutableStateOf(originalPhoto?.secondPrinter?.value ?: false) }
    var supportEnabled by remember(detail) { mutableStateOf(originalPhoto?.supportEnabled?.value ?: false) }
    var extraPrints by remember(detail) { mutableStateOf(originalPhoto?.extraPrints?.value ?: false) }
    var dsgvo by remember(detail) { mutableStateOf(originalPhoto?.dsgvo?.value ?: false) }

    val positions = remember(detail) {
        mutableStateListOf<EditablePosition>().apply {
            originalRental?.positions.orEmpty().forEachIndexed { index, p ->
                add(
                    EditablePosition(
                        key = "existing-${p.id}-$index",
                        rentalItemId = p.rentalItemId,
                        itemName = p.itemName,
                        variant = p.variantSnapshot,
                        quantity = formatNumber(p.quantity),
                        unitPrice = formatNumber(p.unitPrice),
                        unitDeposit = formatNumber(p.unitDeposit),
                        notes = p.notes
                    )
                )
            }
        }
    }

    var addItemOpen by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    val oldCalculated = originalRental?.positions.orEmpty().sumOf { it.lineTotal.takeIf { total -> total > 0 } ?: (it.quantity * it.unitPrice) }
    val newCalculated = positions.sumOf { parseNumber(it.quantity) * parseNumber(it.unitPrice) }
    val rentalPreview = when {
        originalRental == null -> 0.0
        originalRental.fixedTotalPrice != null -> (originalRental.fixedTotalPrice + (newCalculated - oldCalculated)).coerceAtLeast(0.0)
        else -> newCalculated
    }
    val photoPreview = if (originalPhoto != null) {
        photoboothPreviewTotal(
            packageName = packageName,
            distanceFee = parseNumber(distanceFee),
            internet = internet,
            secondPrinter = secondPrinter,
            dsgvo = dsgvo,
            supportEnabled = supportEnabled,
            supportHours = parseNumber(supportHours),
            extraPrints = extraPrints
        )
    } else 0.0
    val combinedPreview = rentalPreview + photoPreview

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Bearbeiten", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(
                "Änderungen werden beim Speichern direkt auf dem Server geprüft und danach neu geladen.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            EditSection("Kunde") {
                EditField("Firma", company) { company = it }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EditField("Vorname", firstName, Modifier.weight(1f)) { firstName = it }
                    EditField("Nachname", lastName, Modifier.weight(1f)) { lastName = it }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EditField("Telefon", phone, Modifier.weight(1f)) { phone = it }
                    EditField("E-Mail", email, Modifier.weight(1f)) { email = it }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EditField("Straße", street, Modifier.weight(1f)) { street = it }
                    EditField("Nr.", houseNo, Modifier.weight(.45f)) { houseNo = it }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EditField("PLZ", postalCode, Modifier.weight(.55f)) { postalCode = it }
                    EditField("Ort", city, Modifier.weight(1f)) { city = it }
                }
                EditField("Kundennotizen", customerNotes, singleLine = false) { customerNotes = it }
            }
        }

        item {
            EditSection("Termin / Auftrag") {
                EditField("Eventdatum (JJJJ-MM-TT)", eventDate) { eventDate = it }
                EditField("Location / Veranstaltungsort", venue) { venue = it }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EditField("Straße", eventStreet, Modifier.weight(1f)) { eventStreet = it }
                    EditField("Nr.", eventHouseNo, Modifier.weight(.45f)) { eventHouseNo = it }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EditField("PLZ", eventPostalCode, Modifier.weight(.55f)) { eventPostalCode = it }
                    EditField("Ort", eventCity, Modifier.weight(1f)) { eventCity = it }
                }
                EditField("Auftragsnotiz", orderNotes, singleLine = false) { orderNotes = it }
            }
        }

        if (originalRental != null) {
            item {
                EditSection("MietMoments · Artikel") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EditField("Abholung", pickupDate, Modifier.weight(1f)) { pickupDate = it }
                        EditField("Event", rentalEventDate, Modifier.weight(1f)) { rentalEventDate = it }
                        EditField("Rückgabe", returnDate, Modifier.weight(1f)) { returnDate = it }
                    }
                    EditField("Mietnotiz", rentalNotes, singleLine = false) { rentalNotes = it }
                    HorizontalDivider()
                    positions.forEachIndexed { index, position ->
                        PositionEditor(
                            position = position,
                            onChange = { changed -> positions[index] = changed },
                            onDelete = { positions.removeAt(index) }
                        )
                    }
                    Button(onClick = { addItemOpen = true }, enabled = !saving) {
                        Icon(Icons.Rounded.Add, null)
                        Text(" Artikel hinzufügen")
                    }
                    PriceRow("MietMoments neu", rentalPreview)
                }
            }
        }

        if (originalPhoto != null) {
            item {
                EditSection("Fotobox") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EditField("Paket", packageName, Modifier.weight(1f)) { packageName = it }
                        EditField("Box", boxName, Modifier.weight(1f)) { boxName = it }
                    }
                    EditField("Hintergrund", backgroundName) { backgroundName = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EditField("Set", setName, Modifier.weight(1f)) { setName = it }
                        EditField("Layout", layoutName, Modifier.weight(1f)) { layoutName = it }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EditField("Start", photoStartDate, Modifier.weight(1f)) { photoStartDate = it }
                        EditField("Ende", photoEndDate, Modifier.weight(1f)) { photoEndDate = it }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EditField("Entfernung km", distanceKm, Modifier.weight(1f)) { distanceKm = it }
                        EditField("Entfernung €", distanceFee, Modifier.weight(1f)) { distanceFee = it }
                    }
                    OptionRow("Internet +20 €", internet) { internet = it }
                    OptionRow("2. Drucker +50 €", secondPrinter) { secondPrinter = it }
                    OptionRow("DSGVO +15 €", dsgvo) { dsgvo = it }
                    OptionRow("+300 Ausdrucke +60 €", extraPrints) { extraPrints = it }
                    OptionRow("Support +20 €/h", supportEnabled) { supportEnabled = it }
                    if (supportEnabled) EditField("Support-Stunden", supportHours) { supportHours = it }
                    PriceRow("Fotobox neu", photoPreview)
                }
            }
        }

        if (originalRental != null && originalPhoto != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Gesamtpreis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            money(combinedPreview),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Wird nach dem Speichern serverseitig nochmals berechnet und sofort neu geladen.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        resultMessage?.let { message ->
            item { Text(message, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onCancel, enabled = !saving, modifier = Modifier.weight(1f)) {
                    Text("Abbrechen")
                }
                Button(
                    onClick = {
                        val editedCustomer = originalCustomer.copy(
                            company = company.trim(),
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            street = street.trim(),
                            houseNo = houseNo.trim(),
                            postalCode = postalCode.trim(),
                            city = city.trim(),
                            notes = customerNotes.trim()
                        )
                        val editedOrder = originalOrder.copy(
                            eventDate = eventDate.trim(),
                            eventStreet = eventStreet.trim(),
                            eventHouseNo = eventHouseNo.trim(),
                            eventPostalCode = eventPostalCode.trim(),
                            eventCity = eventCity.trim(),
                            venue = venue.trim(),
                            notes = orderNotes.trim()
                        )
                        val editedRental = originalRental?.copy(
                            pickupDate = pickupDate.trim(),
                            eventDate = rentalEventDate.trim().ifBlank { eventDate.trim() },
                            returnDate = returnDate.trim(),
                            notes = rentalNotes.trim(),
                            positions = positions.mapIndexed { index, p ->
                                val qty = parseNumber(p.quantity).coerceAtLeast(.01)
                                val price = parseNumber(p.unitPrice).coerceAtLeast(0.0)
                                RentalPositionDto(
                                    id = 0,
                                    rentalItemId = p.rentalItemId,
                                    itemName = p.itemName.trim(),
                                    variantSnapshot = p.variant.trim(),
                                    quantity = qty,
                                    unitPrice = price,
                                    unitDeposit = parseNumber(p.unitDeposit).coerceAtLeast(0.0),
                                    lineTotal = qty * price,
                                    notes = p.notes.trim()
                                )
                            }
                        )
                        val editedPhoto = originalPhoto?.copy(
                            packageName = packageName.trim(),
                            backgroundName = backgroundName.trim(),
                            boxName = boxName.trim(),
                            setName = setName.trim(),
                            layoutName = layoutName.trim(),
                            startDate = photoStartDate.trim(),
                            endDate = photoEndDate.trim(),
                            totalPrice = photoPreview,
                            internetOption = FlexibleBoolean(internet),
                            secondPrinter = FlexibleBoolean(secondPrinter),
                            supportEnabled = FlexibleBoolean(supportEnabled),
                            supportHours = parseNumber(supportHours),
                            extraPrints = FlexibleBoolean(extraPrints),
                            dsgvo = FlexibleBoolean(dsgvo),
                            distanceKm = parseNumber(distanceKm),
                            distanceFee = parseNumber(distanceFee)
                        )
                        onSave(editedCustomer, editedOrder, editedRental, editedPhoto) { ok, message ->
                            resultMessage = message
                            if (ok) onCancel()
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.weight(1f)
                ) {
                    if (saving) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                    else Icon(Icons.Rounded.Save, null)
                    Text(if (saving) " Speichert …" else " Speichern")
                }
            }
        }
    }

    if (addItemOpen) {
        AddItemDialog(
            catalogItems = catalogItems,
            onDismiss = { addItemOpen = false },
            onAdd = { item ->
                val existingIndex = positions.indexOfFirst { it.rentalItemId == item.id && it.variant == item.variant }
                if (existingIndex >= 0) {
                    val current = positions[existingIndex]
                    val qty = parseNumber(current.quantity) + 1.0
                    positions[existingIndex] = current.copy(quantity = formatNumber(qty))
                } else {
                    positions.add(
                        EditablePosition(
                            key = "new-${item.id}-${positions.size}",
                            rentalItemId = item.id,
                            itemName = item.name,
                            variant = item.variant,
                            quantity = "1",
                            unitPrice = formatNumber(item.unitPrice),
                            unitDeposit = formatNumber(item.deposit),
                            notes = ""
                        )
                    )
                }
                addItemOpen = false
            }
        )
    }
}

@Composable
private fun EditSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .94f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun EditField(
    label: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    singleLine: Boolean = true,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun OptionRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(1f))
        Checkbox(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun PositionEditor(
    position: EditablePosition,
    onChange: (EditablePosition) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(position.itemName, fontWeight = FontWeight.Bold)
                    if (position.variant.isNotBlank()) Text(position.variant, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, "Artikel entfernen") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EditField("Menge", position.quantity, Modifier.weight(.65f)) { onChange(position.copy(quantity = it)) }
                EditField("Preis €", position.unitPrice, Modifier.weight(1f)) { onChange(position.copy(unitPrice = it)) }
            }
            EditField("Positionsnotiz", position.notes, singleLine = false) { onChange(position.copy(notes = it)) }
            Text(
                "Position: ${money(parseNumber(position.quantity) * parseNumber(position.unitPrice))}",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PriceRow(label: String, value: Double) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(money(value), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun AddItemDialog(
    catalogItems: List<ItemDto>,
    onDismiss: () -> Unit,
    onAdd: (ItemDto) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(catalogItems, query) {
        val q = query.trim().lowercase()
        if (q.isBlank()) catalogItems.take(80)
        else catalogItems.filter {
            listOf(it.name, it.sku, it.category, it.variant)
                .joinToString(" ")
                .lowercase()
                .contains(q)
        }.take(80)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Artikel hinzufügen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Artikel suchen") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(filtered, key = { it.id }) { item ->
                        TextButton(onClick = { onAdd(item) }, modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.name, fontWeight = FontWeight.SemiBold)
                                    val meta = listOf(item.sku, item.variant).filter { it.isNotBlank() }.joinToString(" · ")
                                    if (meta.isNotBlank()) Text(meta, style = MaterialTheme.typography.bodySmall)
                                }
                                Text(money(item.unitPrice), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Schließen") } }
    )
}

private fun parseNumber(raw: String): Double {
    val value = raw.trim().replace("€", "").replace(" ", "")
    if (value.isBlank()) return 0.0
    val normalized = if (value.contains(',')) value.replace(".", "").replace(',', '.') else value
    return normalized.toDoubleOrNull() ?: 0.0
}

private fun formatNumber(value: Double): String =
    if (abs(value - value.toInt()) < .0001) value.toInt().toString()
    else String.format(Locale.GERMANY, "%.2f", value)

private fun photoboothPreviewTotal(
    packageName: String,
    distanceFee: Double,
    internet: Boolean,
    secondPrinter: Boolean,
    dsgvo: Boolean,
    supportEnabled: Boolean,
    supportHours: Double,
    extraPrints: Boolean
): Double {
    val packagePrice = when (packageName.trim()) {
        "Gold" -> 149.0
        "Rubin" -> 199.0
        "Saphir" -> 249.0
        "Smaragd" -> 299.0
        "Diamant" -> 349.0
        else -> 0.0
    }
    return packagePrice +
        distanceFee.coerceAtLeast(0.0) +
        (if (internet) 20.0 else 0.0) +
        (if (secondPrinter) 50.0 else 0.0) +
        (if (dsgvo) 15.0 else 0.0) +
        (if (supportEnabled) supportHours.coerceAtLeast(0.0) * 20.0 else 0.0) +
        (if (extraPrints) 60.0 else 0.0)
}

private fun money(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale.GERMANY).format(value)
