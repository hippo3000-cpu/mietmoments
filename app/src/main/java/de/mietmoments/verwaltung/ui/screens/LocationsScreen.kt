package de.mietmoments.verwaltung.ui.screens

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
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
            val hay = listOf(loc.name, loc.street, loc.houseNo, loc.postalCode, loc.city, loc.contactName, loc.contactPhone, loc.contactEmail, loc.homepage, loc.accessInfo, loc.notes).joinToString(" ").lowercase()
            tokens.all { hay.contains(it) }
        }
    }
    LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Locations", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text("${locations.size} Orte im Verzeichnis", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Rounded.Search, null) }, label = { Text("Name, Straße, PLZ, Ort oder Kontakt") })
        }
        items(rows, key = { it.id }) { loc ->
            Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(loc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (loc.displayAddress.isNotBlank()) Text(loc.displayAddress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (loc.contactName.isNotBlank()) Text(listOf(loc.contactName, loc.contactPhone).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.labelMedium)
                    }
                    if (loc.displayAddress.isNotBlank()) IconButton(onClick = { onNavigate(loc.displayAddress) }) { Icon(Icons.Rounded.Navigation, "Navigation") }
                }
            }
        }
    }
}
