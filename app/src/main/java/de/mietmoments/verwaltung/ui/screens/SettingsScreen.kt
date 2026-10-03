package de.mietmoments.verwaltung.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.mietmoments.verwaltung.BuildConfig
import de.mietmoments.verwaltung.data.AppSettings
import de.mietmoments.verwaltung.ui.components.MomoMascot

@Composable
fun SettingsScreen(settings: AppSettings, onPreferences: (Boolean, Boolean) -> Unit) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Einstellungen", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text(
            "Aussehen, Momo und die Verbindung zu deiner MietMoments-Verwaltung.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (settings.momoEnabled) {
            MomoMascot(
                "Ich bin übrigens abschaltbar. Persönlich nehme ich das natürlich überhaupt nicht.",
                animated = settings.animationsEnabled
            )
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    Box(
                        Modifier
                            .size(42.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Security, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Sichere Verbindung", fontWeight = FontWeight.Bold)
                        Text(
                            settings.serverUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Der App-Schlüssel liegt verschlüsselt im Android Keystore.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.AutoAwesome, null, tint = MaterialTheme.colorScheme.secondary)
                    Text("App-Erlebnis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                SettingSwitch(
                    "Momo",
                    "Maskottchen, Statushinweise und kleine Sprüche anzeigen",
                    settings.momoEnabled
                ) { onPreferences(it, settings.animationsEnabled) }

                SettingSwitch(
                    "Animationen",
                    "Bewegungen, Übergänge und Momo-Animationen",
                    settings.animationsEnabled
                ) { onPreferences(settings.momoEnabled, it) }
            }
        }

        Text(
            "MietMoments ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
