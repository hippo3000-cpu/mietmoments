package de.mietmoments.verwaltung.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import de.mietmoments.verwaltung.data.ItemDto
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ItemsScreen(itemsData: List<ItemDto>) {
    var query by remember { mutableStateOf("") }
    val q = query.trim().lowercase()
    val rows = remember(itemsData, q) {
        itemsData.filter { q.isBlank() || listOf(it.name, it.sku, it.category, it.variant, it.storageLocation).joinToString(" ").lowercase().contains(q) }
    }
    LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Artikel", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text("${itemsData.size} aktive Artikel", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Rounded.Search, null) }, label = { Text("Artikel, SKU, Kategorie, Lagerort") })
        }
        items(rows.take(500), key = { it.id }) { item ->
            Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text(money(item.unitPrice), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    val meta = listOf(item.sku, item.category, item.variant).filter { it.isNotBlank() }.joinToString(" · ")
                    if (meta.isNotBlank()) Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (item.storageLocation.isNotBlank() || item.stock != null) {
                        Text(listOf(item.storageLocation.takeIf { it.isNotBlank() }?.let { "Lager: $it" }, item.stock?.let { "Bestand: ${smartNumber(it)}" }).filterNotNull().joinToString(" · "), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

private fun money(v: Double): String = NumberFormat.getCurrencyInstance(Locale.GERMANY).format(v)
private fun smartNumber(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else String.format(Locale.GERMANY, "%.2f", v)
