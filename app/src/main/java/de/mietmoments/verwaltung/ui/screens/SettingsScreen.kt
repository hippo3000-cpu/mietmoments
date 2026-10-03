package de.mietmoments.verwaltung.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.mietmoments.verwaltung.data.AppSettings
import de.mietmoments.verwaltung.ui.components.MomoMascot

@Composable
fun SettingsScreen(settings: AppSettings, onPreferences: (Boolean, Boolean) -> Unit) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Einstellungen", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        if (settings.momoEnabled) MomoMascot("Ich bin übrigens abschaltbar. Persönlich nehme ich das natürlich überhaupt nicht.", animated = settings.animationsEnabled)
        Card {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.Security, null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Server", fontWeight = FontWeight.Bold)
                        Text(settings.serverUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("App-Schlüssel ist verschlüsselt auf diesem Gerät gespeichert.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                SettingSwitch("Momo", "Maskottchen und kleine Hinweise anzeigen", settings.momoEnabled) { onPreferences(it, settings.animationsEnabled) }
                SettingSwitch("Animationen", "Übergänge und Momo-Bewegungen", settings.animationsEnabled) { onPreferences(settings.momoEnabled, it) }
            }
        }
    }
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
