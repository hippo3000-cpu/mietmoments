# Audit der bisherigen MietMoments APK (v1.10)

Geprüft am 03.10.2026 anhand der zuletzt erzeugten APK aus dem Verwaltungs-Update v8.05.

## Ist-Zustand

- Paket: `de.mietmoments.verwaltung`
- Aufbau: Android-Wrapper + WebView
- UI: lokale `assets/index.html`, `assets/app.css`, `assets/app.js`
- Backend: `https://mietmoments.de/verwaltung/mobile_api.php`
- Endpunkte: `health`, `snapshot`, `customer`, `save`
- Offline: eigene Queue im Android-Wrapper, UI-Cache per localStorage
- API-Authentifizierung: statischer `X-MM-Mobile-Token` im APK
- Signaturzertifikat SHA-256: `AD:C3:7E:26:A8:6D:2E:B4:00:57:11:29:85:CF:F3:98:2C:B5:E4:5B:09:44:AD:08:0D:49:89:9B:4C:61:02:AF`

## Warum v2 nativ wird

Die WebView-Lösung war für schnelle Iterationen praktisch, koppelt aber Darstellung, Cache und Geschäftslogik eng an HTML/JavaScript. Die neue App trennt UI, Datenzugriff und lokale Speicherung sauber und nutzt Kotlin + Jetpack Compose.

## Sicherheit

Der vorhandene API-Schlüssel wird absichtlich **nicht** in dieses öffentliche Repository übernommen. v2 speichert den Schlüssel nach einmaliger Einrichtung mit Android Keystore AES/GCM lokal auf dem Gerät.

## Update-Hinweis

Der private Signierschlüssel der alten APK liegt nicht im Repository vor. Deshalb kann ein neu signierter Release-Build die bestehende produktive APK nicht direkt überschreiben. Debug-Builds benutzen zusätzlich den Application-ID-Suffix `.next` und können parallel installiert werden. Für den späteren produktiven Wechsel ist einmalig eine kontrollierte Migration/Neuinstallation nötig; danach wird ein dauerhafter Release-Key über GitHub Secrets verwendet.
