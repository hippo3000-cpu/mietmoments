package de.mietmoments.verwaltung.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Note
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.mietmoments.verwaltung.data.CustomerDetailResponse
import de.mietmoments.verwaltung.ui.components.MomoMascot
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CustomerDetailScreen(
    detail: CustomerDetailResponse?,
    loading: Boolean,
    momoEnabled: Boolean,
    animations: Boolean,
    onCall: (String) -> Unit,
    onMail: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    if (detail == null) {
        Column(Modifier.padding(24.dp)) {
            if (loading) CircularProgressIndicator()
            Text(if (loading) "Lade Kundendaten …" else "Keine Kundendaten geladen.", Modifier.padding(top = 12.dp))
        }
        return
    }
    val c = detail.customer
    val selected = detail.selected
    LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(c.displayName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            if (c.customerNo.isNotBlank()) Text(c.customerNo, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                if (c.phone.isNotBlank()) AssistChip(onClick = { onCall(c.phone) }, label = { Text(c.phone) }, leadingIcon = { Icon(Icons.Rounded.Call, null) })
                if (c.email.isNotBlank()) AssistChip(onClick = { onMail(c.email) }, label = { Text("E-Mail") }, leadingIcon = { Icon(Icons.Rounded.Email, null) })
            }
        }
        if (c.notes.isNotBlank()) {
            item {
                DetailCard(Icons.Rounded.Note, "Kundennotizen", "Interne Hinweise") {
                    Text(c.notes, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        selected.order?.let { order ->
            item {
                DetailCard(Icons.Rounded.Navigation, "Termin", order.orderNo) {
                    if (order.eventDate.isNotBlank()) KeyValue("Eventdatum", prettyDate(order.eventDate))
                    if (order.venue.isNotBlank()) KeyValue("Location", order.venue)
                    if (order.eventAddress.isNotBlank()) {
                        AssistChip(onClick = { onNavigate(order.eventAddress) }, label = { Text(order.eventAddress) }, leadingIcon = { Icon(Icons.Rounded.Navigation, null) })
                    }
                    if (order.notes.isNotBlank()) KeyValue("Auftragsnotiz", order.notes)
                }
            }
        }
        selected.rental?.let { rental ->
            item {
                DetailCard(Icons.Rounded.Inventory2, "MietMoments", rental.rentalPeriodLabel.ifBlank { "Mietartikel" }) {
                    if (rental.rentalPeriodLabel.isNotBlank()) KeyValue("Mietzeitraum", rental.rentalPeriodLabel + if (rental.rentalPeriodDays > 0) " · ${rental.rentalPeriodDays} Tage" else "")
                    KeyValue("Summe", money(rental.total))
                    if (rental.notes.isNotBlank()) KeyValue("Hinweis", rental.notes)
                }
            }
            items(rental.positions, key = { "position-${it.id}-${it.itemName}" }) { p ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .35f))) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("${smart(p.quantity)}× ${p.itemName}", fontWeight = FontWeight.SemiBold)
                            if (p.variantSnapshot.isNotBlank()) Text(p.variantSnapshot, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (p.notes.isNotBlank()) Text(p.notes, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(money(p.lineTotal), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        selected.photobooth?.let { photo ->
            item {
                DetailCard(Icons.Rounded.PhotoCamera, "Fotobox", photo.packageName.ifBlank { "Fotobox" }) {
                    if (photo.boxName.isNotBlank()) KeyValue("Box", photo.boxName)
                    if (photo.backgroundName.isNotBlank()) KeyValue("Hintergrund", photo.backgroundName)
                    if (photo.setName.isNotBlank()) KeyValue("Set", photo.setName)
                    if (photo.layoutName.isNotBlank()) KeyValue("Layout", photo.layoutName)
                    if (photo.totalPrice > 0) KeyValue("Summe", money(photo.totalPrice))
                    val extras = buildList {
                        if (photo.internetOption.value) add("Internet")
                        if (photo.secondPrinter.value) add("2. Drucker")
                        if (photo.extraPrints.value) add("+300 Ausdrucke")
                        if (photo.dsgvo.value) add("DSGVO")
                    }
                    if (extras.isNotEmpty()) KeyValue("Extras", extras.joinToString(" · "))
                }
            }
        }
        selected.location?.let { loc ->
            item {
                DetailCard(Icons.Rounded.Navigation, loc.name.ifBlank { "Location" }, "Vor Ort") {
                    if (loc.displayAddress.isNotBlank()) AssistChip(onClick = { onNavigate(loc.displayAddress) }, label = { Text(loc.displayAddress) }, leadingIcon = { Icon(Icons.Rounded.Navigation, null) })
                    if (loc.contactName.isNotBlank()) KeyValue("Kontakt", listOf(loc.contactName, loc.contactPhone).filter { it.isNotBlank() }.joinToString(" · "))
                    if (loc.accessInfo.isNotBlank()) KeyValue("Zufahrt / Aufbau", loc.accessInfo)
                    if (loc.notes.isNotBlank()) KeyValue("Location-Notiz", loc.notes)
                }
            }
        }
        if (momoEnabled) item { MomoMascot("Kundennotizen gelesen? Sehr gut. Genau dort wohnt meistens der Satz, den später alle suchen.", animated = animations, compact = true) }
    }
}

@Composable
private fun DetailCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            content()
        }
    }
}

@Composable
private fun KeyValue(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun prettyDate(raw: String): String = runCatching { LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN)) }.getOrDefault(raw)
private fun money(v: Double): String = NumberFormat.getCurrencyInstance(Locale.GERMANY).format(v)
private fun smart(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else String.format(Locale.GERMANY, "%.2f", v)
