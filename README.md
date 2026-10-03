# MietMoments Android

Native Android-App für die MietMoments-Verwaltung.

## MietMoments 2.0.0 · Elegant Magic + Milo

Die App ist eine echte native Android-App mit **Kotlin + Jetpack Compose + Material 3** und bleibt bewusst getrennt von der Partnervermietungs-/Erich-App.

### Architektur
- Paket: `de.mietmoments.verwaltung`
- Kotlin + Jetpack Compose
- Material 3
- verschlüsselter App-Schlüssel über Android Keystore
- kurzer Einmal-Kopplungscode statt manuellem API-Schlüssel
- lokaler JSON-Cache für schnelle/offline lesbare Ansichten
- Server-API über `mobile_api.php`
- GitHub Actions für reproduzierbare APK-Builds
- Java 17 / Android minSdk 26 / targetSdk 36

### Elegant Magic
- heller Weiß-/Creme-/Champagner-Hintergrund
- dezenter animierter Glitzer und weiche Lichtreflexe
- warme Gold-/Rosé-Akzente
- moderne Karten und klare mobile Navigation
- Animationen können in den Einstellungen deaktiviert werden

### Milo
**Milo** ist ein kleines Baby-Nilpferd im Tamagotchi-Stil. Er ist kein festes Textmodul, sondern bewegt sich dezent frei über die App. Je nach Zustand läuft, hüpft, springt, schläft, grinst oder schaut er traurig bzw. neugierig. Gelegentlich erscheint für kurze Zeit eine kleine Sprechblase. Milo reagiert unter anderem auf Synchronisierung, Offline-Zustand und volle Wochen.

Milo kann in den Einstellungen komplett deaktiviert werden.

### Funktionen
- Dashboard
- Wochenplan
- Kundensuche
- Kundendetails inklusive Kundennotizen
- MietMoments- und Fotobox-Details
- Artikel und Lager
- Locations
- Online-/Offline- und Sync-Status
- sichere App-Kopplung

### Build
GitHub Actions baut Debug- und Release-Varianten. Lokal:

```bash
gradle :app:assembleDebug
gradle :app:assembleRelease
```
