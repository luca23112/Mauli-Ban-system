# MauliBanSystem

Ein vollständiges Paper-Plugin für Minecraft 1.21 mit GUI-basierten Bann-Gründen, temporären und permanenten Banns, Historie, MySQL-/Datei-Speicherung und Discord-Webhook-Logs.

## Features
- `/ban <spieler>` öffnet ein zweistufiges GUI für Grund und Dauer.
- `/pban <spieler> <grund>` für permanente Banns.
- `/tempban <spieler> <zeit> <grund>` für temporäre Banns.
- `/unban <spieler>` hebt aktive Banns auf.
- `/check <spieler>` zeigt den aktiven Bann an.
- `/history <spieler>` zeigt die Bann-Historie an.
- Datei-Speicherung als Fallback, optional MySQL für Netzwerkbetrieb.
- Automatisches Entbannen nach Ablauf.
- Discord Webhook Logs und Staff-Broadcast.

## Kompilieren mit Maven
1. Stelle sicher, dass **Java 21** und **Maven 3.9+** installiert sind.
2. Führe im Projektordner aus:
   ```bash
   mvn clean package
   ```
3. Die fertige Plugin-Datei liegt anschließend unter `target/MauliBanSystem-1.0.0.jar`.

## Installation
1. Kopiere die JAR in den `plugins/`-Ordner deines Paper-1.21-Servers.
2. Starte den Server einmal, damit `config.yml` erzeugt wird.
3. Passe MySQL-, Webhook- und Nachrichten-Einstellungen in `plugins/MauliBanSystem/config.yml` an.
4. Starte oder lade den Server neu.

## Hinweise
- Für Netzwerkbetrieb `storage.type: MYSQL` setzen.
- Für lokale Speicherung `storage.type: FILE` verwenden.
- Discord Webhook ist optional und bleibt deaktiviert, wenn keine URL gesetzt ist.
