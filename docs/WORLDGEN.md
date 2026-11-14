# NEXUS-BAY – Worldgen-Katalog

> Status: Design-Spezifikation. Die hier beschriebenen Inselprofile sind geplant und noch nicht vollständig im Laufzeit-Generator implementiert. Der aktuelle IslandService erzeugt derzeit nur eine generische Starterinsel.

## Ziel

Jede Insel soll eine erkennbare Identität aus One Piece erhalten: eigene Silhouette, Küstenlinie, Höhenprofil, Vegetation, Baustile, Landmarken und später passende Gegner-, NPC- und Boss-Zonen. Inseln werden ohne Spieler-Befehl im Rahmen der Welt-/Archipel-Generierung angelegt. Admin-Befehle bleiben Werkzeuge zum Testen und Verwalten, nicht Voraussetzung für Boss-Spawns.

## Inselkatalog

| Insel / Gebiet | Meeresregion | Terrain- und Biomprofil | Prägende Landmarken |
|---|---|---|---|
| Tequila Wolf | East Blue | Riesige, langgestreckte Brückeninsel; Industrie- und Baustellenzone | Endlos wirkende Brücke, Gerüste, Arbeiterlager, Wachposten |
| Spider Miles | West Blue | Dichte Industriestadt, graue Küste und Hafenbecken | Fabriken, Schornsteine, Lagerhäuser, Werft |
| Minion Island | North Blue | Kaltes, windiges Waldgebiet mit Schnee- und Ruinenflächen | Verschneite Lichtungen, verfallene Gebäude, alte Wege |
| Torino Kingdom | South Blue | Üppiger Tropendschungel mit übergroßen Bäumen | Riesige Baumkronen, Nester, Dschungelpfade, Dorfplattformen |
| Jaya | Grand Line | Dschungel und dicht bebaute Piratenhafenstadt | Mock Town, Kneipen, Markt, Ruinen und versteckte Schätze |
| Little Garden | Grand Line | Urzeitlicher Dschungel, Sümpfe und vulkanische Höhen | Riesige Farne, Dinosaurier-Reviere, alte Kampfplätze, Vulkane |
| Loguetown | East Blue | Große Hafenstadt mit Straßenraster und Marktvierteln | Hinrichtungsplattform, Marinebasis, Hafen, Marktplatz |
| Ohara | West Blue | Bewaldete Insel mit archäologischen Ruinen | Baum der Allwissenheit, Bibliotheksruinen, Forschungsgelände |
| Flevance | North Blue | Helle Stadtarchitektur, verlassene Straßen und überwucherte Randzonen | Krankenhaus, Plätze, Ruinen und Minen-/Industriespuren |
| Amazon Lily | Calm Belt | Abgeschiedene, dicht bewaldete Insel mit steilen Felsen | Amazonenstadt, Schlangenstatuen, Trainingsarena, Wasserfälle |
| Enies Lobby | Grand Line | Gerichtsfestung auf hoher Felsplattform mit steilen Wasserfällen | Haupttor, Gerichtsgebäude, Marineanlagen, Brücken |
| Impel Down | Grand Line | Unterseeischer Festungskomplex; vertikale Dungeon-Generierung statt normaler Oberflächeninsel | Mehrere Gefängnisebenen, Zellenblöcke, Kontrollräume, Bosskammern |
| Marineford | Grand Line | Militärische Festungsinsel mit zentralem offenem Schlachtfeld | Marine-Hauptquartier, Platz, Mauern, Tore, Geschützstellungen |
| Thriller Bark | Florian Triangle | Riesiges düsteres Schiff-/Inselmotiv mit Friedhof und Spukwald | Geisterschiff, Friedhof, Herrenhaus, Nebelwald |
| Dressrosa | New World | Mediterrane Küste, helle Stadt und zerklüftetes Hinterland | Königspalast, Kolosseum, Stadtviertel, Spielzeugfabrik |
| Zou | New World | Bewegliches Insel-Ökosystem auf einem riesigen Elefanten | Wald auf dem Rücken, Mokomo-Dorf, Wasserfälle, Zugangspunkte |
| Whole Cake Island | New World | Süßigkeiten-Fantasy-Biom mit ungewöhnlichen Farben und Formen | Kuchen-/Süßigkeitenlandschaft, Totland-Siedlungen, Schloss |
| Wano Kuni | New World | Abgeschlossene Region mit klar getrennten Landschaftszonen | Blumenhauptstadt, Dörfer, Wälder, Berge, Küsten und Schreine |
| Onigashima | Wano-Region | Separate, felsige Raid-Insel mit Burgplateau | Schädelkuppel, Burg, Höhlen, Raid-Arena |
| Punk Hazard | New World | Zweigeteilte Extremzone: Eis/Schnee und Feuer/Lava | Labor, gefrorene Küste, Lavafelder, zerstörte Forschungsanlagen |

## Generator-Regeln

1. **Datengetrieben:** Jedes Profil erhält eine stabile ID, Region, Biom-/Materialpalette, Höhenparameter, Küstenform, Vegetationsregeln und Landmarken-Pool. Keine Insel soll nur durch ihren Anzeigenamen erkannt werden.
2. **Reproduzierbar:** Ein gespeicherter Seed und eine feste Insel-ID müssen bei Regeneration dieselbe Grundform und dieselben Landmarken liefern.
3. **Räumliche Trennung:** Insel- und Struktur-Bounding-Boxes dürfen sich nicht überschneiden. Besondere Gebiete wie Impel Down und Onigashima können als Instanz-/Nebenwelt oder verknüpfte Teilregion umgesetzt werden.
4. **Generierungsreihenfolge:** Meeresboden und Küste → Geländeform → Biome und Oberflächen → Vegetation → Landmarken → Wege und Spawnpunkte → Schutz-/Bosszonen.
5. **Performance:** Große Bauwerke in begrenzte Chunks/Abschnitte aufteilen; keine vollständige Welt in einem einzigen synchronen Tick generieren. Bereits generierte Inseln werden gespeichert und nicht bei jedem Neustart neu gebaut.
6. **Spielbarkeit:** Sichere Ankunftszone, Dock/Ankerpunkt, Rückreise-/Navigationspunkt und barrierearme Hauptwege vorsehen.
7. **Crossplay:** Für den Paper-Server ausschließlich Bukkit/Paper-Blöcke und -Entitäten sowie pluginbasierte Mechaniken verwenden. Forge-/Fabric-Mod-Inhalte sind nicht Teil derselben Paper-Server-Installation.

## Automatische Boss- und Quest-Anbindung (spätere Ausbaustufe)

- Bossdefinitionen erhalten Insel-ID, Spawnzone, Spawnbedingungen, Respawn-Regel, Phasen, Fähigkeiten, Loot und Schutz vor Mehrfachspawn.
- Bosse erscheinen automatisch an ihren festgelegten Orten, sobald die Insel initialisiert ist bzw. die Spawnbedingungen erfüllt sind. Kein Spieler muss einen Spawn-Befehl ausführen.
- NPC-Questgeber werden später als Inselinhalt registriert. Quests referenzieren Insel-ID und Landmarken-ID, damit Dialoge und Ziele nicht an zufälligen Koordinaten hängen.
- Boss-Arenen und Questbereiche werden beim Generieren als geschützte Zonen markiert.

## Umsetzung in Releases

- **Worldgen 2.4 – Neue Weltregionen:** Dressrosa, Zou, Whole Cake Island, Wano Kuni, Onigashima und Punk Hazard als vollständige Profile spezifizieren.
- **Worldgen 2.5 – Datenmodell:** Inselprofile in einer versionierten YAML-Datei ablegen und beim Pluginstart validieren.
- **Worldgen 2.6 – Terrain-Engine:** Profile im IslandService tatsächlich auswerten; unterschiedliche Silhouetten, Höhen, Oberflächen und Vegetation generieren.
- **Worldgen 2.7 – Landmarken:** Struktur-Pools und sichere, reproduzierbare Platzierung implementieren.
- **Worldgen 2.8 – Persistenz & Performance:** Seed, Status, Bounding-Boxes und Generierungsfortschritt speichern; Chunkweise Generierung.
- **Gameplay danach:** Insel-NPCs und Questketten, danach Gegner- und Boss-Systeme mit automatischen Spawnzonen.

## Aktueller Implementierungsstand

Der vorhandene `IslandService` baut eine generische runde Insel mit Gras-/Erd-/Steinschichten, Wasser-Ring, kleinem Dock, Leuchtturm und Kiste. Er liest derzeit nur `settings.island-spacing` und `settings.island-radius`; er wertet noch keine der oben genannten Inselprofile aus. Diese Spezifikation dokumentiert daher das Zielbild und ist keine Behauptung, dass die Inseln bereits im Spiel gebaut werden.
