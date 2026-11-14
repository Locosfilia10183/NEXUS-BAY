# NEXUS-BAY Befehle

**Server-Adresse:** `nexus-bay.duckdns.org` (Java Port 25565, Bedrock 19132)

## Spieler
| Befehl | Beschreibung |
|--------|--------------|
| `/bayprofile` | Level, XP, Bay-Coins |
| `/baypay <spieler> <betrag>` | Coins senden |
| `/baycrew` | Crew-Info |
| `/baycrew create <name>` | Crew gründen |
| `/baycrew invite <spieler>` | Einladen |
| `/baycrew accept` | Einladung annehmen |
| `/baycrew leave` | Crew verlassen |
| `/baycrew deposit <betrag>` | In Crew-Bank einzahlen |
| `/bayskill list <tree>` | Skillbaum (swords, pistols, harpoons, crew) |
| `/bayskill unlock <tree> <node>` | Skill freischalten |
| `/bayquest` / `/bayquest list` | Alle Quests |
| `/bayquest status` | Eigene angenommene Quests |
| `/bayquest accept <code>` | Quest annehmen (z.B. `first_blood`) |
| `/bayquest abandon <code>` | Quest abbrechen |
| `/bayisland visit <name>` | Insel besuchen |
| `/bayisland list` | Inseln auflisten |

## Admin
| Befehl | Permission |
|--------|------------|
| `/bayadmin` | `nexusbay.admin` |
| `/bayinv <spieler>` | `nexusbay.admin` |
| `/bayisland create <name>` | `nexusbay.admin` |

## Hinweis
Crews, Skills und Quests brauchen `database.enabled: true` und MariaDB.
