package de.mietmoments.verwaltung.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.mietmoments.verwaltung.data.EventDto
import de.mietmoments.verwaltung.ui.components.MomoMascot
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WeekScreen(
    weekStart: LocalDate,
    events: List<EventDto>,
    momoEnabled: Boolean,
    animations: Boolean,
    onPrev: () -> Unit,
    onToday: () -> Unit,
    onNext: () -> Unit,
    onOpenCustomer: (Int, Int) -> Unit,
    onNavigate: (String) -> Unit
) {
    val end = weekStart.plusDays(6)
    val grouped = events.sortedWith(compareBy<EventDto> { it.date }.thenBy { it.customer }).groupBy { it.date }
    LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onPrev) { Icon(Icons.Rounded.ChevronLeft, "Vorherige Woche") }
                Column {
                    Text("Wochenplan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("${fmtShort(weekStart)} – ${fmt(end)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onNext) { Icon(Icons.Rounded.ChevronRight, "Nächste Woche") }
            }
            TextButton(onClick = onToday) { Text("Zur aktuellen Woche") }
        }
        if (events.isEmpty()) {
            item {
                if (momoEnabled) MomoMascot("Nichts geplant. Ich werte das als offiziell genehmigte Kabelbinder-Pause.", animated = animations)
                else Text("Keine Termine in dieser Woche.", Modifier.padding(12.dp))
            }
        }
        grouped.forEach { (date, dayEvents) ->
            item(key = "day-$date") {
                Text(fmtLong(date), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            items(dayEvents, key = { "${it.orderId}-${it.title}-${it.date}" }) { event ->
                EventCard(event, onOpenCustomer, onNavigate)
            }
        }
    }
}

@Composable
private fun EventCard(event: EventDto, onOpenCustomer: (Int, Int) -> Unit, onNavigate: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpenCustomer(event.customerId, event.orderId) },
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(event.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                ServiceChip(event.source)
            }
            Text(event.customer, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (event.customerNo.isNotBlank()) Text(event.customerNo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (event.detail.isNotBlank()) Text(event.detail, style = MaterialTheme.typography.bodyMedium)
            if (event.address.isNotBlank()) {
                AssistChip(
                    onClick = { onNavigate(event.address) },
                    label = { Text(event.address) },
                    leadingIcon = { Icon(Icons.Rounded.Navigation, null) }
                )
            }
        }
    }
}

@Composable
private fun ServiceChip(source: String) {
    val label = when (source) {
        "both" -> "Fotobox + MM"
        "photo" -> "Fotobox"
        "mm" -> "MietMoments"
        else -> "Termin"
    }
    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
}

private fun fmt(d: LocalDate) = d.format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN))
private fun fmtShort(d: LocalDate) = d.format(DateTimeFormatter.ofPattern("dd.MM.", Locale.GERMAN))
private fun fmtLong(raw: String) = runCatching { LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", Locale.GERMAN)) }.getOrDefault(raw)
