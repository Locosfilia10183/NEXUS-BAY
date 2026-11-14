# NEXUS-BAY Rangsystem

## Standardränge

| Rang        | Farbe     | Gewicht | Kurzbeschreibung              |
|-------------|-----------|---------|-------------------------------|
| Spieler     | Grau      | 10      | Standard                      |
| VIP         | Grün      | 20      | Basis-Premium                 |
| VIP+        | Grün      | 30      | Erweitertes Premium           |
| MVP         | Aqua      | 40      | Höheres Premium               |
| MVP+        | Aqua      | 50      | Top-Premium                   |
| LEGEND      | Gold      | 60      | Spezieller Premium-Rang       |
| YouTuber    | Rot       | 70      | Content-Creator               |
| Supporter   | Blau      | 80      | Team – Support                |
| Moderator   | Dunkelgrün| 90      | Team – Moderation             |
| Admin       | Rot       | 100     | Team – Administration         |
| Owner       | Dunkelrot | 110     | Vollzugriff                   |

## Technische Umsetzung

- **Permission-System:** LuckPerms (Proxy + alle Paper-Server)
- **Synchronisation:** Über die zentrale Datenbank oder LuckPerms Messaging
- **Chat-/Tab-Präfixe:** Über LuckPerms Meta + TAB oder eigenes Plugin
- **Temporäre Ränge:** LuckPerms Temporary Permissions

Beispiel-Gruppen liegen unter `config/luckperms-groups.yml.example`.

## Admin-Befehle (LuckPerms)

```text
/lp user <spieler> parent set <rang>
/lp user <spieler> parent addtemp <rang> 30d
/lp creategroup <name>
/lp group <name> permission set <node> true
```

Nur Benutzer mit entsprechender Permission dürfen Ränge vergeben.
