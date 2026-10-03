package de.mietmoments.verwaltung.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import de.mietmoments.verwaltung.data.CustomerDto
import java.text.Normalizer

@Composable
fun CustomersScreen(customers: List<CustomerDto>, onOpenCustomer: (Int, Int) -> Unit) {
    var query by remember { mutableStateOf("") }
    val rows = remember(customers, query) {
        customers
            .map { it to scoreCustomer(it, query) }
            .filter { query.isBlank() || it.second > 0 }
            .sortedWith(compareByDescending<Pair<CustomerDto, Int>> { it.second }.thenBy { it.first.displayName })
            .map { it.first }
    }

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Kunden finden", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text(
                    "${customers.size} Kunden · Namen, Orte und Kontaktdaten sofort durchsuchen",
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
                placeholder = { Text("z. B. Müller Saarlouis oder 0176 …") },
                shape = RoundedCornerShape(22.dp)
            )
        }

        item {
            AnimatedVisibility(query.isNotBlank()) {
                Text(
                    "${rows.size} Treffer",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (rows.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f))
                ) {
                    Text(
                        "Kein Treffer. Versuch einen Teil von Name, Firma, Ort, Telefonnummer oder E-Mail.",
                        Modifier.padding(18.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(rows.take(300), key = { it.id }) { customer ->
            CustomerCard(customer = customer, onClick = { onOpenCustomer(customer.id, customer.orderId) })
        }
    }
}

@Composable
private fun CustomerCard(customer: CustomerDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .animateContentSize()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .90f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            Modifier.padding(15.dp),
            horizontalArrangement = Arrangement.spacedBy(13.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (customer.company.isNotBlank()) Icons.Rounded.Business else Icons.Rounded.Person,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        customer.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (customer.customerNo.isNotBlank()) {
                        Text(
                            customer.customerNo,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                val meta = listOf(customer.city, customer.phone, customer.email)
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
                if (meta.isNotBlank()) {
                    Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    if (customer.hasMm.value) ServicePill("MietMoments", false)
                    if (customer.hasPhoto.value) ServicePill("Fotobox", true)
                }
            }
        }
    }
}

@Composable
private fun ServicePill(text: String, accent: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (accent) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text,
            Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (accent) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun scoreCustomer(c: CustomerDto, raw: String): Int {
    if (raw.isBlank()) return c.id
    val tokens = normalize(raw).split(' ').filter { it.isNotBlank() }
    val fields = listOf(
        c.displayName, c.company, c.firstName, c.lastName, c.customerNo,
        c.city, c.postalCode, c.phone, c.email, c.address
    ).map(::normalize)
    var score = 0
    for (token in tokens) {
        val exact = fields.indexOfFirst { it == token }
        val prefix = fields.indexOfFirst { it.startsWith(token) }
        val contains = fields.indexOfFirst { it.contains(token) }
        when {
            exact >= 0 -> score += 40
            prefix >= 0 -> score += 25
            contains >= 0 -> score += 15
            fields.any { fuzzyOneEdit(it, token) } -> score += 6
            else -> return 0
        }
    }
    return score + if (normalize(c.displayName).startsWith(normalize(raw))) 20 else 0
}

private fun normalize(s: String): String = Normalizer.normalize(
    s.lowercase().replace("ß", "ss").replace("ä", "ae").replace("ö", "oe").replace("ü", "ue"),
    Normalizer.Form.NFD
)
    .replace("\\p{M}+".toRegex(), "")
    .replace("[^a-z0-9]+".toRegex(), " ")
    .trim()

private fun fuzzyOneEdit(field: String, token: String): Boolean {
    if (token.length < 4) return false
    return field.split(' ').any { word ->
        if (kotlin.math.abs(word.length - token.length) > 1) false else levenshteinAtMostOne(word, token)
    }
}

private fun levenshteinAtMostOne(a: String, b: String): Boolean {
    if (a == b) return true
    if (kotlin.math.abs(a.length - b.length) > 1) return false
    var i = 0
    var j = 0
    var edits = 0
    while (i < a.length && j < b.length) {
        if (a[i] == b[j]) {
            i++
            j++
            continue
        }
        if (++edits > 1) return false
        when {
            a.length > b.length -> i++
            b.length > a.length -> j++
            else -> {
                i++
                j++
            }
        }
    }
    if (i < a.length || j < b.length) edits++
    return edits <= 1
}
