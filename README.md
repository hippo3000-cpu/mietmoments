# MietMoments Android v2

Die neue native Android-App für MietMoments. Der vorherige WebView-Client wird schrittweise durch eine moderne Kotlin-/Jetpack-Compose-App ersetzt.

## Was bereits enthalten ist

- modernes Material-3-Design mit Light/Dark Mode
- animierter Startbereich und Seitenzustände
- **Momo**, das optionale MietMoments-Maskottchen mit situationsabhängigen Sprüchen
- Wochenplan mit Wochenwechsel und direkter Kundendetailansicht
- Kundenliste mit fehlertoleranter Suche
- Kundendetailansicht inklusive **Kundennotizen**, MietMoments, Fotobox und Location
- Artikelübersicht inkl. Bestand/Lagerort
- Location-Suche und direkte Navigation
- manueller Sync plus lokaler Snapshot-/Kundencache
- sichere Ersteinrichtung: API-Schlüssel wird nicht im Repository hinterlegt, sondern per Android Keystore verschlüsselt
- GitHub Actions Debug-Build

## Entwicklungsbranch

`feature/native-compose-v2`

Debug-Builds verwenden `de.mietmoments.verwaltung.next` und können parallel zur bisherigen produktiven APK installiert werden.

## Server-Verbindung

Die App nutzt zunächst die bestehende API:

`https://mietmoments.de/verwaltung/mobile_api.php`

Beim ersten Start werden Serveradresse und App-Schlüssel eingetragen. Der Schlüssel wird nicht in GitHub gespeichert.

## Build

Das Repository braucht lokal keinen fest eingebetteten Server-Schlüssel.

```bash
gradle :app:assembleDebug
```

GitHub Actions erzeugt bei jedem Push auf den Entwicklungsbranch ein Debug-APK als Workflow-Artefakt.

Weitere Details: [Architektur](docs/ARCHITECTURE.md) · [Audit der alten APK](docs/APK_AUDIT.md)
