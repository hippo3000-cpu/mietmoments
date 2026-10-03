package de.mietmoments.verwaltung.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.mietmoments.verwaltung.data.EventDto
import de.mietmoments.verwaltung.data.SnapshotResponse
import de.mietmoments.verwaltung.ui.components.MomoMascot
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun DashboardScreen(
    snapshot: SnapshotResponse?,
    online: Boolean,
    momoEnabled: Boolean,
    animations: Boolean,
    onOpenWeek: () -> Unit,
    onOpenCustomers: () -> Unit,
    onOpenItems: () -> Unit,
    onOpenLocations: () -> Unit,
    onOpenCustomer: (Int, Int) -> Unit
) {
    val events = snapshot?.events.orEmpty().sortedBy { it.date }
    val phrase = when {
        !online -> "Kein Netz? Kein Drama. Ich halte die Stellung und zeige dir, was schon da ist."
        events.isEmpty() -> "Diese Woche ist verdächtig ruhig. Sogar die Kabelbinder entspannen sich."
        events.size >= 8 -> "Da kommt was auf uns zu. Ich habe die Kisten innerlich schon beschriftet."
        else -> "Alles im Blick. Du machst die Events, ich passe auf das Chaos auf."
    }

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Heute im Blick", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.GERMAN)), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (momoEnabled) item { MomoMascot(phrase, animated = animations, modifier = Modifier.fillMaxWidth()) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Kunden", snapshot?.customerCount ?: snapshot?.customers?.size ?: 0, Icons.Rounded.People, Modifier.weight(1f), onOpenCustomers)
                StatCard("Artikel", snapshot?.itemCount ?: snapshot?.items?.size ?: 0, Icons.Rounded.Inventory2, Modifier.weight(1f), onOpenItems)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Termine", events.size, Icons.Rounded.CalendarMonth, Modifier.weight(1f), onOpenWeek)
                StatCard("Locations", snapshot?.locationCount ?: snapshot?.locations?.size ?: 0, Icons.Rounded.Place, Modifier.weight(1f), onOpenLocations)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text("Als Nächstes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        if (events.isEmpty()) {
            item { EmptyCard("Keine Termine in dieser Woche") }
        } else {
            items(events.take(5), key = { "${it.orderId}-${it.date}-${it.title}" }) { event ->
                UpcomingCard(event, onOpenCustomer)
            }
        }
    }
}

@Composable
private fun StatCard(title: String, count: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(15.dp).animateContentSize()) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text(count.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun UpcomingCard(event: EventDto, onOpenCustomer: (Int, Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onOpenCustomer(event.customerId, event.orderId) }) {
        Column(Modifier.padding(14.dp)) {
            Text(formatDate(event.date), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(event.customer.ifBlank { "Termin" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(event.title, style = MaterialTheme.typography.bodyMedium)
            if (event.detail.isNotBlank()) Text(event.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyCard(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f))) {
        Text(text, Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatDate(raw: String): String = runCatching {
    LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("EEE, dd.MM.yyyy", Locale.GERMAN))
}.getOrDefault(raw)
