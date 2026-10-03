package de.mietmoments.verwaltung.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.mietmoments.verwaltung.data.LocationDto

@Composable
fun LocationsScreen(locations: List<LocationDto>, onNavigate: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val tokens = query.trim().lowercase().split(' ').filter { it.isNotBlank() }
    val rows = remember(locations, query) {
        locations.filter { loc ->
            val hay = listOf(
                loc.name, loc.street, loc.houseNo, loc.postalCode, loc.city,
                loc.contactName, loc.contactPhone, loc.contactEmail, loc.homepage,
                loc.accessInfo, loc.notes
            ).joinToString(" ").lowercase()
            tokens.all { hay.contains(it) }
        }
    }

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Locations", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text(
                    "${locations.size} Orte · Adresse, Kontakt und Aufbauhinweise sofort griffbereit",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                placeholder = { Text("Name, Straße, PLZ, Ort oder Kontakt") },
                shape = RoundedCornerShape(22.dp)
            )
        }

        items(rows, key = { it.id }) { loc ->
            Card(
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .90f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    Modifier.padding(15.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Place, null, tint = MaterialTheme.colorScheme.primary)
                    }

                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(loc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        if (loc.displayAddress.isNotBlank()) {
                            Text(
                                loc.displayAddress,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (loc.contactName.isNotBlank()) {
                            Text(
                                listOf(loc.contactName, loc.contactPhone).filter { it.isNotBlank() }.joinToString(" · "),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            if (loc.wifiSsid.isNotBlank()) {
                                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
                                    Row(
                                        Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Rounded.Wifi, null, Modifier.size(15.dp))
                                        Text("WLAN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (loc.accessInfo.isNotBlank()) {
                                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Text(
                                        "Aufbauhinweis",
                                        Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (loc.accessInfo.isNotBlank()) {
                            Text(
                                loc.accessInfo,
                                maxLines = 2,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (loc.displayAddress.isNotBlank()) {
                        IconButton(onClick = { onNavigate(loc.displayAddress) }) {
                            Icon(Icons.Rounded.Navigation, "Navigation", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
