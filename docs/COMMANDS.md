# NEXUS-BAY Befehle

**Adresse:** `nexus-bay.duckdns.org` (Java 25565 · Bedrock 19132)

## Quests (Kopfgeld)
| Befehl | Funktion |
|--------|----------|
| `/bayquest` | Übersicht |
| `/bayquest accept` | Kopfgeld annehmen (10 Monster → 100 Coins + 50 XP) |
| `/bayquest status` | Fortschritt |
| `/bayquest abandon` | Abbrechen |

Fortschritt wird in der Plugin-Config (YAML) gespeichert – **keine Datenbank nötig**.

## Spieler
| Befehl | Beschreibung |
|--------|--------------|
| `/bayprofile` | Level, XP, Bay-Coins |
| `/baypay <spieler> <betrag>` | Coins senden |
| `/bayisland visit <name>` | Insel besuchen |
| `/bayisland list` | Inseln |

## Admin
| Befehl | Permission |
|--------|------------|
| `/bayadmin` | `nexusbay.admin` |
| `/bayinv <spieler>` | `nexusbay.admin` |
| `/bayisland create <name>` | `nexusbay.admin` |
