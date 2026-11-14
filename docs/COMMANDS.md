# NEXUS-BAY Befehle

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
| `/bayskill list <tree>` | Skillbaum anzeigen (swords, pistols, harpoons, crew) |
| `/bayskill unlock <tree> <node>` | Skill freischalten |
| `/bayquest` / `/bayquest list` | Logbuch |
| `/bayquest accept <code>` | Quest annehmen |
| `/bayisland visit <name>` | Insel besuchen |
| `/bayisland list` | Inseln auflisten |

## Admin
| Befehl | Permission |
|--------|------------|
| `/bayadmin` | `nexusbay.admin` |
| `/bayinv <spieler>` | `nexusbay.admin` |
| `/bayisland create <name>` | `nexusbay.admin` |

## Hinweis
Crews, Skills und Quests benötigen `database.enabled: true` und eine laufende MariaDB.
