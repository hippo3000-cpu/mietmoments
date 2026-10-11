# Milo 3D Production Pack V1

**Ziel:** Das vierbeinige, rosa Baby-Nilpferd aus den vier Originalbildern wird als **echtes 360°-drehbares 3D-Modell**, nicht als PNG-Abfolge, in MietMoments integriert. Die Referenzbilder befinden sich unverändert im separaten Downloadpaket `Milo_Production_Pack_V1.zip`, nicht im Repository.

## Verbindliche Identität
- Kopf überproportional groß, kompakter Baby-Körper, **vier kurze dicke Beine** und helle runde Pfotennägel
- Große dunkle glänzende Augen mit rosa Irisreflexen, weißen Catchlights, Wimpern und weichen Brauen
- Kurze breite hellrosa Schnauze, zwei kleine Nasenlöcher, kleiner Mund mit Zunge/Zähnchen
- Kleine runde Ohren, Haarwirbel, kurzer Schwanz mit Quaste
- Warmrosa glatte weich glänzende Haut, heller Bauch; **kein graues Nilpferd, kein Teddy, kein anderes Hippo**
- Die vier Vorlagen sind eine Referenzsammlung mit Variationen: **ein** neutrales 3D-Master-Modell festlegen und erst nach Sichtfreigabe riggen.

## Liefergegenstände
1. **Identitätsfreigabe:** echte Ansichten **derselben Figur** von vorn, Seite, 3/4 und hinten; 360°-Turntable.
2. **Editierbares 3D:** `milo_v1.blend` (Mesh, UV, Textur, PBR-Material, Skelett), `milo_v1.glb` (Android-Runtime, gebackene Animationen).
3. **Rig:** Root/Pelvis/Spine/Head/Jaw, 4 Gliedmaßen mit Pfoten, Ohren, Schwanz, Augen/Brauen.
4. **Mimik:** Morph Targets `blink_L`, `blink_R`, `smile`, `sad`, `mouth_open`, `sniff`; erweiterte Ziele siehe Downloadpaket.
5. **Animationen:** `idle_breathe`, `blink`, `look_around`, `sniff_once`, `ear_wiggle`, `walk_loop`, `trot_loop`, `hop_once`, `sit_down`, `sit_idle`, `lie_down`, `sleep_loop`, `wake_up`, `happy_once`, `sad_idle`, `wave_once`, `turn_in_place`.

## Runtime-Vertrag
- Lokalachsen: **+Y oben, +Z vorne, +X rechts**; Touch-Drag dreht das **wirkliche Modell** um Y.
- **Eingaben:** Tap → Freude/Ohrwackeln/Winken; Drag → 360°-Rotation; kein Blockieren von Listen-Scrollen.
- **App-Ereignisse:** SyncStart → Walk; SyncSuccess → Happy; SyncError → Sad; Offline → Look; BusyWeek → Hop; IdleTimeout → Sleep.
- **Rendering-Vorschlag:** Filament/glTF in `AndroidView` eingebettet in Jetpack Compose, konkrete APIs/Abhängigkeiten vor Implementierung testen.
- **Fallback:** Vorhandenes `MiloCompanion.kt` (derzeit Compose-Zeichnung) bleibt, bis die 3D-Variante optisch und technisch freigegeben ist; Einstellungen Milo aus / Animationen aus respektieren.
- Perf-Ziel: ruckelfrei auf Android-Mittelklasse, Grafik optional reduzieren; GPU-Ressourcen beim App-Lifecycle freigeben.

## Abnahme: Vier Gates
- **Gate A – Ähnlichkeit:** Benutzer bestätigt: *Das ist mein Milo*, einschließlich Seiten-/Rückansicht. Keine Fremdfigur.
- **Gate B – Echtes 3D:** Das **gleiche** Mesh lässt sich 360° drehen. Keine gerenderten Bildwechsel.
- **Gate C – Leben:** Atmen, Blinzeln, Vierpfotengang, Hüpfen, Sitzen, Liegen, Schlafen, Emotionen mit glatten Blends.
- **Gate D – App:** Integration, Touch, Echtzeit-Sync, kleine Displays, animiert/aus, Test-APK auf Gerät.

## Aktueller Stand
**Nur Spezifikation.** Ein fertiges `milo_v1.glb` / `milo_v1.blend` existiert hier noch **nicht**, und die Kotlin-App ist noch nicht auf einen 3D-Renderer umgestellt. Das ausführliche Produktionspaket mit den Originalbildern, Animation-Manifesten und Beispiel-Interface liegt separat als ZIP vor.

Hinweis: Der bestehende Entwicklungsbranch `feature/edit-prices-and-item-management-20261010` enthält `ui/components/MiloCompanion.kt` mit Compose-Animation, aber kein fertiges Modell.
