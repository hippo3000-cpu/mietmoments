# MietMoments Android

Native Android-App für die MietMoments-Verwaltung.

## Modern Momo v3

Die App wird als echte native Android-App mit **Kotlin + Jetpack Compose + Material 3** entwickelt. Sie ist bewusst getrennt von der Partnervermietungs-/Erich-App.

### Architektur
- Paket: `de.mietmoments.verwaltung`
- Kotlin + Jetpack Compose
- Material 3
- verschlüsselter App-Schlüssel über Android Keystore
- lokaler JSON-Cache für schnelle/offline lesbare Ansichten
- Server-API bleibt kompatibel zu `mobile_api.php`
- GitHub Actions erzeugt reproduzierbare Debug-APKs
- Java 17 / Android minSdk 26 / targetSdk 36

### Oberfläche
- modernes MietMoments-Farbsystem mit Hell-/Dunkelmodus
- animiertes Dashboard und fließende Navigation
- Wochenplan, Kunden, Artikel und Locations nativ
- sichtbarer Online-/Sync-Status
- Animationen können in den Einstellungen deaktiviert werden

### Momo
**Momo** ist das MietMoments-Maskottchen. Es reagiert auf den App-Zustand (offline, synchronisieren, ruhige oder volle Woche), bewegt sich dezent und liefert beim Antippen wechselnde kleine Sprüche. Momo kann komplett deaktiviert werden.

### Branches
- `main`: stabiler Stand
- `feature/native-compose-v2`: ursprüngliche Compose-Migration
- `feature/modern-momo-v3`: modernes UI/UX und Momo-Ausbau

### Build
GitHub Actions baut die App bei Änderungen automatisch. Lokal:

```bash
gradle :app:assembleDebug
```

Die Debug-APK liegt danach unter `app/build/outputs/apk/debug/`.
