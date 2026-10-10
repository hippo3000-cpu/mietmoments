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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.mietmoments.verwaltung.data.ItemDto
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ItemsScreen(
    itemsData: List<ItemDto>,
    saving: Boolean,
    onSaveItem: (ItemDto, (Boolean, String) -> Unit) -> Unit,
    onArchiveItem: (ItemDto, (Boolean, String) -> Unit) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<ItemDto?>(null) }
    val q = query.trim().lowercase()
    val rows = remember(itemsData, q) {
        itemsData.filter {
            q.isBlank() || listOf(it.name, it.sku, it.category, it.variant, it.storageLocation)
                .joinToString(" ")
                .lowercase()
                .contains(q)
        }
    }

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Artikel & Lager", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    IconButton(onClick = { editing = ItemDto() }, enabled = !saving) {
                        Icon(Icons.Rounded.Add, "Artikel hinzufügen", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Text(
                    "${itemsData.size} aktive Artikel · Preis, Variante und Lagerort auf einen Blick",
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
                placeholder = { Text("Artikel, SKU, Kategorie oder Lagerort") },
                shape = RoundedCornerShape(22.dp)
            )
        }

        items(rows.take(500), key = { it.id }) { item ->
            Card(
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .90f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    Modifier.padding(15.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Inventory2, null, tint = MaterialTheme.colorScheme.primary)
                    }

                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                item.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                money(item.unitPrice),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black
                            )
                        }

                        val meta = listOf(item.sku, item.category, item.variant)
                            .filter { it.isNotBlank() }
                            .joinToString(" · ")
                        if (meta.isNotBlank()) {
                            Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            if (item.storageLocation.isNotBlank()) InfoPill("Lager: ${item.storageLocation}")
                            item.stock?.let { InfoPill("Bestand: ${smartNumber(it)}") }
                        }
                    }

                    IconButton(onClick = { editing = item }, enabled = !saving) {
                        Icon(Icons.Rounded.Edit, "Artikel bearbeiten", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    editing?.let { item ->
        EditItemDialog(
            item = item,
            saving = saving,
            onDismiss = { if (!saving) editing = null },
            onSave = { changed, callback ->
                onSaveItem(changed) { ok, message ->
                    callback(ok, message)
                    if (ok) editing = null
                }
            },
            onArchive = { selected, callback ->
                onArchiveItem(selected) { ok, message ->
                    callback(ok, message)
                    if (ok) editing = null
                }
            }
        )
    }
}

@Composable
private fun EditItemDialog(
    item: ItemDto,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (ItemDto, (Boolean, String) -> Unit) -> Unit,
    onArchive: (ItemDto, (Boolean, String) -> Unit) -> Unit
) {
    var name by remember(item) { mutableStateOf(item.name) }
    var sku by remember(item) { mutableStateOf(item.sku) }
    var category by remember(item) { mutableStateOf(item.category) }
    var variant by remember(item) { mutableStateOf(item.variant) }
    var unit by remember(item) { mutableStateOf(item.unit) }
    var priceType by remember(item) { mutableStateOf(item.priceType) }
    var unitPrice by remember(item) { mutableStateOf(decimal(item.unitPrice)) }
    var deposit by remember(item) { mutableStateOf(decimal(item.deposit)) }
    var storage by remember(item) { mutableStateOf(item.storageLocation) }
    var stock by remember(item) { mutableStateOf(item.stock?.let(::decimal).orEmpty()) }
    var message by remember(item) { mutableStateOf("") }
    var confirmArchive by remember(item) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item.id == 0) "Artikel hinzufügen" else "Artikel bearbeiten") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallEditField("Name", name) { name = it }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallEditField("SKU", sku, Modifier.weight(1f)) { sku = it }
                    SmallEditField("Kategorie", category, Modifier.weight(1f)) { category = it }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallEditField("Variante", variant, Modifier.weight(1f)) { variant = it }
                    SmallEditField("Einheit", unit, Modifier.weight(1f)) { unit = it }
                }
                SmallEditField("Preistyp", priceType) { priceType = it }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallEditField("Preis €", unitPrice, Modifier.weight(1f)) { unitPrice = it }
                    SmallEditField("Kaution €", deposit, Modifier.weight(1f)) { deposit = it }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallEditField("Lagerort", storage, Modifier.weight(1f)) { storage = it }
                    SmallEditField("Bestand", stock, Modifier.weight(1f)) { stock = it }
                }
                Text("Aktueller Preis: ${money(parseDecimal(unitPrice))}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                if (message.isNotBlank()) Text(message, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving && name.isNotBlank(),
                onClick = {
                    val changed = item.copy(
                        name = name.trim(),
                        sku = sku.trim(),
                        category = category.trim(),
                        variant = variant.trim(),
                        unit = unit.trim(),
                        priceType = priceType.trim(),
                        unitPrice = parseDecimal(unitPrice),
                        deposit = parseDecimal(deposit),
                        storageLocation = storage.trim(),
                        stock = stock.trim().takeIf { it.isNotEmpty() }?.let(::parseDecimal)
                    )
                    onSave(changed) { _, msg -> message = msg }
                }
            ) { Text(if (saving) "Speichert …" else "Speichern") }
        },
        dismissButton = {
            Row {
                if (item.id > 0) TextButton(onClick = { confirmArchive = true }, enabled = !saving) {
                    Icon(Icons.Rounded.Delete, "Artikel entfernen")
                    Text(" Entfernen")
                }
                TextButton(onClick = onDismiss, enabled = !saving) { Text("Abbrechen") }
            }
        }
    )
    if (confirmArchive) {
        AlertDialog(
            onDismissRequest = { if (!saving) confirmArchive = false },
            title = { Text("Artikel entfernen?") },
            text = { Text("Der Artikel verschwindet aus dem Katalog. Bereits gebuchte Positionen und ihre Preise bleiben bestehen.") },
            confirmButton = {
                TextButton(enabled = !saving, onClick = {
                    onArchive(item) { ok, result ->
                        message = result
                        if (ok) confirmArchive = false
                    }
                }) { Text("Artikel archivieren") }
            },
            dismissButton = { TextButton(onClick = { confirmArchive = false }, enabled = !saving) { Text("Abbrechen") } }
        )
    }
}

@Composable
private fun SmallEditField(label: String, value: String, modifier: Modifier = Modifier.fillMaxWidth(), onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true
    )
}

@Composable
private fun InfoPill(text: String) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(
            text,
            Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun parseDecimal(value: String): Double = value.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
private fun decimal(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.GERMANY, "%.2f", value)
private fun money(v: Double): String = NumberFormat.getCurrencyInstance(Locale.GERMANY).format(v)
private fun smartNumber(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else String.format(Locale.GERMANY, "%.2f", v)
