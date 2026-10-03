package de.mietmoments.verwaltung.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.mietmoments.verwaltung.data.AppSettings
import de.mietmoments.verwaltung.ui.components.MomoMascot

@Composable
fun SetupScreen(
    initial: AppSettings,
    busy: Boolean,
    onSave: (String, String, Boolean, Boolean, (Boolean, String) -> Unit) -> Unit
) {
    var server by remember { mutableStateOf(initial.serverUrl) }
    var pairCode by remember { mutableStateOf("") }
    var momo by remember { mutableStateOf(initial.momoEnabled) }
    var animations by remember { mutableStateOf(initial.animationsEnabled) }
    var message by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("MietMoments", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
        Text("Einmal koppeln. Danach einfach benutzen.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(18.dp))

        if (momo) {
            MomoMascot(
                "Hol dir in der Verwaltung einen Kopplungscode. Ich kümmere mich um den langen Technik-Kram.",
                animated = animations
            )
        }

        Spacer(Modifier.height(18.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Mit Verwaltung koppeln", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "In der Verwaltung: System → MietMoments App → Neuen Kopplungscode erzeugen. Der Code gilt 10 Minuten und nur einmal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = server,
                    onValueChange = { server = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Serveradresse") },
                    leadingIcon = { Icon(Icons.Rounded.Storage, null) },
                    singleLine = true,
                    supportingText = { Text("Standard: https://mietmoments.de/verwaltung/") }
                )

                OutlinedTextField(
                    value = pairCode,
                    onValueChange = { raw ->
                        val clean = raw.uppercase().replace(Regex("[^A-Z0-9]"), "").take(8)
                        pairCode = if (clean.length > 4) clean.take(4) + "-" + clean.drop(4) else clean
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Kopplungscode") },
                    leadingIcon = { Icon(Icons.Rounded.Link, null) },
                    singleLine = true,
                    placeholder = { Text("ABCD-EFGH") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Ascii
                    ),
                    supportingText = { Text("Kein App-Schlüssel mehr nötig.") }
                )

                PreferenceRow("Momo anzeigen", "Maskottchen, Hinweise und kleine Sprüche", momo) { momo = it }
                PreferenceRow("Animationen", "Bewegungen und Übergänge in der App", animations) { animations = it }

                AnimatedVisibility(message.isNotBlank()) {
                    Text(
                        message,
                        color = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = {
                        onSave(server, pairCode, momo, animations) { ok, text ->
                            success = ok
                            message = text
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Rounded.Lock, null)
                        Text("  Sicher koppeln")
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceRow(title: String, text: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
