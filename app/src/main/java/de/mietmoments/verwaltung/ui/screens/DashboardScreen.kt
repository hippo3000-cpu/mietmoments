package de.mietmoments.verwaltung.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
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
    syncing: Boolean,
    onSync: () -> Unit,
    onOpenWeek: () -> Unit,
    onOpenCustomers: () -> Unit,
    onOpenItems: () -> Unit,
    onOpenLocations: () -> Unit,
    onOpenCustomer: (Int, Int) -> Unit
) {
    val events = snapshot?.events.orEmpty().sortedBy { it.date }
    val phrase = when {
        !online -> "Netz weg. Ruhe bewahren. Ich habe den letzten Stand noch im Rucksack."
        syncing -> "Ich sortiere gerade Daten. Bitte nicht am Kabel ziehen."
        events.isEmpty() -> "Diese Woche ist verdächtig ruhig. Sogar die Kabelbinder entspannen sich."
        events.size >= 8 -> "Da kommt was auf uns zu. Ich habe die Kisten innerlich schon beschriftet."
        else -> "Alles im Blick. Du machst die Events, ich passe auf das Chaos auf."
    }

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HeroHeader(
                online = online,
                syncing = syncing,
                onSync = onSync,
                eventCount = events.size
            )
        }

        if (momoEnabled) {
            item {
                MomoMascot(
                    message = phrase,
                    animated = animations,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Text(
                "Schnellzugriff",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
        }

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
            Spacer(Modifier.height(2.dp))
            Text("Als Nächstes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
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
private fun HeroHeader(
    online: Boolean,
    syncing: Boolean,
    onSync: () -> Unit,
    eventCount: Int
) {
    val rotation by animateFloatAsState(if (syncing) 360f else 0f, label = "hero-sync")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                ),
                RoundedCornerShape(30.dp)
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.GERMAN)),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .84f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Heute läuft.",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                if (eventCount == 0) "Keine Termine in dieser Woche."
                else "$eventCount Termin${if (eventCount == 1) "" else "e"} in dieser Woche.",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .90f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = .14f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            if (online) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                            null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            if (online) "Online" else "Offline",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Button(
                    onClick = onSync,
                    enabled = !syncing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Rounded.Refresh, null, Modifier.rotate(rotation))
                    Text(if (syncing) "  Abgleich …" else "  Aktualisieren")
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    icon: ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp).animateContentSize()) {
            Box(
                Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp))
                    .padding(9.dp)
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(12.dp))
            Text(count.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun UpcomingCard(event: EventDto, onOpenCustomer: (Int, Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenCustomer(event.customerId, event.orderId) },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.padding(15.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    formatDateCompact(event.date),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(event.customer.ifBlank { "Termin" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(event.title, style = MaterialTheme.typography.bodyMedium)
                if (event.detail.isNotBlank()) {
                    Text(event.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun EmptyCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Text(text, Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatDateCompact(raw: String): String = runCatching {
    LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("dd.MM.", Locale.GERMAN))
}.getOrDefault(raw)
