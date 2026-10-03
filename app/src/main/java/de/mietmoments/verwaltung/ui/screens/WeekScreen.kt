package de.mietmoments.verwaltung.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.mietmoments.verwaltung.data.EventDto
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
    val grouped = events
        .sortedWith(compareBy<EventDto> { it.date }.thenBy { it.customer })
        .groupBy { it.date }

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            WeekHeader(
                weekStart = weekStart,
                end = end,
                count = events.size,
                onPrev = onPrev,
                onToday = onToday,
                onNext = onNext
            )
        }

        if (events.isEmpty()) {
            item {
                Text("Keine Termine in dieser Woche.", Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        grouped.forEach { (date, dayEvents) ->
            item(key = "day-$date") {
                DayHeader(date = date, count = dayEvents.size)
            }
            items(dayEvents, key = { "${it.orderId}-${it.title}-${it.date}" }) { event ->
                EventCard(event, onOpenCustomer, onNavigate)
            }
        }
    }
}

@Composable
private fun WeekHeader(
    weekStart: LocalDate,
    end: LocalDate,
    count: Int,
    onPrev: () -> Unit,
    onToday: () -> Unit,
    onNext: () -> Unit
) {
    val headerShape = RoundedCornerShape(28.dp)
    Card(
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = .26f), headerShape),
        shape = headerShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .78f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPrev) { Icon(Icons.Rounded.ChevronLeft, "Vorherige Woche") }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Wochenplan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(
                        "${fmtShort(weekStart)} – ${fmt(end)}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                IconButton(onClick = onNext) { Icon(Icons.Rounded.ChevronRight, "Nächste Woche") }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface.copy(alpha = .75f)) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.CalendarMonth, null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            "$count Termin${if (count == 1) "" else "e"}",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                TextButton(onClick = onToday) { Text("Aktuelle Woche") }
            }
        }
    }
}

@Composable
private fun DayHeader(date: String, count: Int) {
    val parsed = runCatching { LocalDate.parse(date) }.getOrNull()
    val today = parsed == LocalDate.now()
    Row(
        Modifier.fillMaxWidth().padding(top = 5.dp, bottom = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        if (today) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
            )
            Column {
                Text(
                    fmtLong(date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (today) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
                if (today) Text("Heute", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
        Text("$count", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EventCard(
    event: EventDto,
    onOpenCustomer: (Int, Int) -> Unit,
    onNavigate: (String) -> Unit
) {
    val eventShape = RoundedCornerShape(22.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenCustomer(event.customerId, event.orderId) }
            .animateContentSize()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, eventShape),
        shape = eventShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .90f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ServiceChip(event.source)
                if (event.customerNo.isNotBlank()) {
                    Text(
                        event.customerNo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                event.customer.ifBlank { "Termin" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (event.title.isNotBlank()) {
                Text(event.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }

            if (event.detail.isNotBlank()) {
                Text(event.detail, style = MaterialTheme.typography.bodyMedium)
            }

            if (event.detailExtra.isNotBlank()) {
                Text(event.detailExtra, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (event.address.isNotBlank()) {
                AssistChip(
                    onClick = { onNavigate(event.address) },
                    label = { Text(event.address, maxLines = 1) },
                    leadingIcon = { Icon(Icons.Rounded.Navigation, null) }
                )
            }
        }
    }
}

@Composable
private fun ServiceChip(source: String) {
    val label = when (source) {
        "both" -> "Fotobox + MietMoments"
        "photo" -> "Fotobox"
        "mm" -> "MietMoments"
        else -> "Termin"
    }
    val accent = source == "photo" || source == "both"
    Surface(
        shape = RoundedCornerShape(50),
        color = if (accent) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            label,
            Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (accent) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

private fun fmt(d: LocalDate) = d.format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN))
private fun fmtShort(d: LocalDate) = d.format(DateTimeFormatter.ofPattern("dd.MM.", Locale.GERMAN))
private fun fmtLong(raw: String) = runCatching {
    LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", Locale.GERMAN))
}.getOrDefault(raw)
