package de.mietmoments.verwaltung.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Note
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.mietmoments.verwaltung.data.CustomerDetailResponse
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
    onNavigate: (String) -> Unit,
    onEdit: () -> Unit
) {
    if (detail == null) {
        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (loading) CircularProgressIndicator()
                Text(if (loading) "Kundendaten kommen …" else "Keine Kundendaten geladen.")
            }
        }
        return
    }

    val c = detail.customer
    val selected = detail.selected

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = .28f), RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = .96f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = .88f),
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .72f)
                            )
                        ),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(end = 42.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Text(
                        c.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (c.customerNo.isNotBlank()) {
                        Text(c.customerNo, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (c.phone.isNotBlank()) {
                            AssistChip(
                                onClick = { onCall(c.phone) },
                                label = { Text("Anrufen") },
                                leadingIcon = { Icon(Icons.Rounded.Call, null) }
                            )
                        }
                        if (c.email.isNotBlank()) {
                            AssistChip(
                                onClick = { onMail(c.email) },
                                label = { Text("E-Mail") },
                                leadingIcon = { Icon(Icons.Rounded.Email, null) }
                            )
                        }
                    }
                }
                IconButton(onClick = onEdit, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(Icons.Rounded.Edit, "Kunde bearbeiten", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (c.notes.isNotBlank()) {
            item {
                DetailCard(
                    icon = Icons.Rounded.Note,
                    title = "Kundennotizen",
                    subtitle = "Interne Hinweise",
                    emphasized = true,
                    onEdit = onEdit
                ) {
                    Text(c.notes, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        selected.order?.let { order ->
            item {
                DetailCard(Icons.Rounded.Navigation, "Termin", order.orderNo, onEdit = onEdit) {
                    if (order.eventDate.isNotBlank()) KeyValue("Eventdatum", prettyDate(order.eventDate))
                    if (order.venue.isNotBlank()) KeyValue("Location", order.venue)
                    if (order.eventAddress.isNotBlank()) {
                        AssistChip(
                            onClick = { onNavigate(order.eventAddress) },
                            label = { Text(order.eventAddress, maxLines = 1) },
                            leadingIcon = { Icon(Icons.Rounded.Navigation, null) }
                        )
                    }
                    if (order.notes.isNotBlank()) KeyValue("Auftragsnotiz", order.notes)
                }
            }
        }

        selected.rental?.let { rental ->
            item {
                DetailCard(Icons.Rounded.Inventory2, "MietMoments", rental.rentalPeriodLabel.ifBlank { "Mietartikel" }, onEdit = onEdit) {
                    if (rental.rentalPeriodLabel.isNotBlank()) {
                        KeyValue(
                            "Mietzeitraum",
                            rental.rentalPeriodLabel + if (rental.rentalPeriodDays > 0) " · ${rental.rentalPeriodDays} Tage" else ""
                        )
                    }
                    KeyValue("Summe", money(rental.total))
                    if (rental.notes.isNotBlank()) KeyValue("Hinweis", rental.notes)
                }
            }

            items(rental.positions, key = { "position-${it.id}-${it.itemName}" }) { p ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .42f)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(13.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("${smart(p.quantity)}× ${p.itemName}", fontWeight = FontWeight.SemiBold)
                            if (p.variantSnapshot.isNotBlank()) {
                                Text(p.variantSnapshot, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (p.notes.isNotBlank()) Text(p.notes, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onEdit) {
                                Icon(Icons.Rounded.Edit, "Position bearbeiten", tint = MaterialTheme.colorScheme.primary)
                            }
                            Text(money(p.lineTotal), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        selected.photobooth?.let { photo ->
            item {
                DetailCard(Icons.Rounded.PhotoCamera, "Fotobox", photo.packageName.ifBlank { "Fotobox" }, onEdit = onEdit) {
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
                    if (extras.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            extras.take(3).forEach { SmallPill(it) }
                        }
                        if (extras.size > 3) SmallPill("+${extras.size - 3} weitere")
                    }
                }
            }
        }

        if (selected.rental != null && selected.photobooth != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Gesamtpreis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Fotobox + MietMoments", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            money(selected.rental.total + selected.photobooth.totalPrice),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        selected.location?.let { loc ->
            item {
                DetailCard(Icons.Rounded.Navigation, loc.name.ifBlank { "Location" }, "Vor Ort", onEdit = onEdit) {
                    if (loc.displayAddress.isNotBlank()) {
                        AssistChip(
                            onClick = { onNavigate(loc.displayAddress) },
                            label = { Text(loc.displayAddress, maxLines = 1) },
                            leadingIcon = { Icon(Icons.Rounded.Navigation, null) }
                        )
                    }
                    if (loc.contactName.isNotBlank()) {
                        KeyValue("Kontakt", listOf(loc.contactName, loc.contactPhone).filter { it.isNotBlank() }.joinToString(" · "))
                    }
                    if (loc.accessInfo.isNotBlank()) KeyValue("Zufahrt / Aufbau", loc.accessInfo)
                    if (loc.notes.isNotBlank()) KeyValue("Location-Notiz", loc.notes)
                }
            }
        }

    }
}

@Composable
private fun DetailCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    emphasized: Boolean = false,
    onEdit: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (emphasized) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .88f)
            else MaterialTheme.colorScheme.surface.copy(alpha = .90f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(38.dp)
                        .background(
                            if (emphasized) MaterialTheme.colorScheme.surface.copy(alpha = .7f) else MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                }
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (subtitle.isNotBlank()) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (onEdit != null) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, "Bearbeiten", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun KeyValue(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SmallPill(text: String) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
        Text(
            text,
            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun prettyDate(raw: String): String = runCatching {
    LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN))
}.getOrDefault(raw)

private fun money(v: Double): String = NumberFormat.getCurrencyInstance(Locale.GERMANY).format(v)
private fun smart(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else String.format(Locale.GERMANY, "%.2f", v)
