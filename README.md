# Teacher Tower Defense

Ein 2D Tower-Defense-Spiel entwickelt mit **Java 25** und **FXGL 17.3** als POS-Abschlussprojekt an der HTL Saalfelden.

## Spielkonzept

Verhindere, dass Schüler das Schulgelände verlassen! Platziere Lehrer strategisch auf der Karte, um die Schüler aufzuhalten, bevor sie den Ausgang erreichen. Verbesserungen, Skins und neue Karten können durch das Sammeln von Münzen und XP freigeschaltet werden.

## Features

- **3 spielbare Lehrer** mit je eigenem Angriffsstil und Projektil
    - Groebl – wirft Fisch-Boomerangs (trifft auf dem Rückweg mehrere Schüler)
    - Feichtner – wirft Alchemie-Potions (Explosionsschaden)
    - Winkler – schießt Floppy Disks (schnelle Feuerrate, viele Ziele)
- **5-2-0 Upgrade-Pfade** pro Lehrer (Pfad A, B, C mit je 5 Stufen)
- **10 Runden** mit 7 verschiedenen Schülertypen (von Erstklässler bis Schulleiter)
- **2 Karten** (HTL Saalfelden, City) mit eigenen Wegstrecken
- **Meta-Progression**: Münzen & XP bleiben zwischen Spielsessions erhalten
- **Lehrer-Skins** kaufbar im Hauptmenü
- **3 Schwierigkeitsgrade** (Easy / Medium / Hard) mit unterschiedlichem Startgeld und Schüler-HP
- **Spielgeschwindigkeit** umschaltbar (1x / 2x)
- **Speichersystem** über Java Preferences API (kein externes File nötig)

## Steuerung

| Aktion | Eingabe |
|---|---|
| Lehrer platzieren | Lehrer im Shop anklicken → auf Karte klicken |
| Lehrer verkaufen | Platzierten Lehrer anklicken → Verkaufen-Button |
| Lehrer upgraden | Platzierten Lehrer anklicken → Upgrade-Panel |
| Runde starten | Start-Button (oder Auto-Start aktivieren) |
| Spielgeschwindigkeit | 1x / 2x Button im Spiel-Menü |
| Menü öffnen | ESC |

## Schülertypen

| Typ | Name | HP | Geschwindigkeit | Besonderheit |
|---|---|---|---|---|
| TYP1 | Erstklässler | 1 | schnell | – |
| TYP2 | Zweitklässler | 2 | schnell | spawnt TYP1 beim Tod |
| TYP3 | Drittklässler | 3 | schnell | spawnt TYP2 beim Tod |
| TYP4 | Viertklässler | 4 | schnell | spawnt TYP3 beim Tod |
| TYP5 | Fünftklässler | 5 | mittel | spawnt 2x TYP4 beim Tod |
| TYP6 | Sechstklässler | 6 | mittel | spawnt 2x TYP5 beim Tod |
| TYP7 | Maturant | 8 | langsam | spawnt 4x TYP5 beim Tod, -3 Leben |
| TYP8 | Schulleiter | 12 | langsam | spawnt 2x TYP7 beim Tod, -5 Leben |


## Technologien

- **Java 25**
- **FXGL 17.3** – Game Framework (Entity-System, Physics, Input, Timer)
- **JavaFX 21.0.6** – UI & Rendering
- **Maven** – Build-System
- **Tiled** – Map-Editor (`.tmx` Dateien)

## Bauen & Starten

```bash
# Kompilieren und starten
./mvnw clean javafx:run

# Nur kompilieren
./mvnw clean compile
