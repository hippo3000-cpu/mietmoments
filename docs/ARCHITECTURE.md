# MietMoments Android v2 – Architektur

## Stack

- Kotlin
- Jetpack Compose / Material 3
- Navigation Compose
- Kotlin Coroutines
- Kotlin Serialization
- Android Keystore für den API-Schlüssel
- AtomicFile für Cache-Dateien
- GitHub Actions für reproduzierbare Debug-Builds

## Schichten

`data/` enthält API, DTOs, sicheren Gerätespeicher und Cache.

`ui/` enthält ViewModel, Navigation, Theme, Screens und Momo.

Die Server-API bleibt zunächst kompatibel mit der bestehenden Verwaltung. Dadurch kann die native App schrittweise ausgebaut werden, ohne den produktiven PHP-Bestand gleichzeitig komplett umzubauen.

## UX-Prinzipien

- Informationen zuerst, Dekoration danach.
- Animationen unterstützen Zustandswechsel und Orientierung.
- Momo ist hilfreich und humorvoll, aber optional.
- Systemweite Einstellung für reduzierte Bewegung wird durch den optionalen App-Schalter ergänzt.
- Dark Mode folgt dem System.

## Nächste technische Ausbaustufen

1. Native Bearbeitung/Speicherung von Kunden und Aufträgen über `action=save`.
2. Persistente Offline-Outbox für Schreibvorgänge.
3. QR-Einrichtung aus der Web-Verwaltung über `mietmoments://setup`.
4. Push-/Reminder-Funktion für Notizen und „Jetzt kümmern“.
5. Packlisten und Lager-Checklisten nativ in der App.
6. Signierter Release-Workflow mit dauerhaftem Keystore in GitHub Secrets.
