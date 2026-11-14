# NEXUS-BAY – Worldgen 2.4 / Phase 1 (Inseln 21–25)

> Status: Umsetzungsplan für die erste spielbare Insel-Testversion. Diese Datei beschreibt konkrete Bauzonen und Generatoranforderungen; sie erzeugt noch keine Blöcke im Spiel.

## Gemeinsame Regeln

- Jede Insel erhält eine feste `id`, einen Seed, einen Mittelpunkt und eine Bounding-Box.
- Die fünf Inseln werden als getrennte Profile generiert. Namen sind Anzeigenamen; die IDs bleiben stabil.
- Pro Insel: sichere Ankunft, Dock/Ankerplatz, erkennbare Haupt-Landmarke, erkundbare Nebenbereiche und ein klarer Rückweg.
- Keine Insel darf innerhalb der Bounding-Box einer anderen Insel oder Struktur liegen.
- Terrain und Bauwerke werden in kleinen Abschnitten generiert, nicht als riesiger synchroner Server-Tick.
- Die Profile verwenden ausschließlich Paper/Bukkit-kompatible Vanilla-Materialien und Plugin-Logik, damit Java- und Bedrock-Spieler dieselbe Welt sehen.

## 21 – Drum Island

**ID:** `drum_island`  
**Region:** Grand Line  
**Silhouette:** Hohe, schneebedeckte Insel mit steilen Felswänden, zentralem Bergmassiv und mehreren kleineren Gipfeln.

| Zone | Vorgesehene Gestaltung |
|---|---|
| Küste | Eisige Buchten, Schnee, Packeis und kleiner Hafen |
| Unterhang | Verschneiter Nadelwald, Serpentinen und verlassene Hütten |
| Mittelhang | Steile Felswände, Brücken und Höhleneingänge |
| Gipfel | Großes Schloss auf einem dominanten Berg |
| Erkundung | Versteckte Höhlen und Aussichtspunkte |

**Materialpalette:** Schnee, Schneeblöcke, Packeis, Fichtenholz, Fichtenstämme, Stein, Andesit, Bruchstein und Laternen.  
**Generatorform:** Mehrere Höhenringe statt flacher Scheibe; Gipfelposition aus Seed ableiten.  
**Landmarken:** Hafen, Bergpfad, Hängebrücke, Schloss.

## 22 – Alabasta

**ID:** `alabasta`  
**Region:** Grand Line  
**Silhouette:** Große Wüstenregion mit Dünen, Felsplateaus, Oasen und einem urbanen Zentrum.

| Zone | Vorgesehene Gestaltung |
|---|---|
| Außenwüste | Sanddünen, vereinzelte Felsen und trockene Schluchten |
| Oasen | Wasserbecken, Palmen-/Akaziengruppen und kleine Lager |
| Stadt | Helle Gebäude, enge Gassen, Markt und Plätze |
| Palastzone | Erhöhtes Plateau mit großem Palastkomplex |
| Ruinen | Halb verschüttete Tempel und Schatzkammern |

**Materialpalette:** Sand, roter Sand, Sandstein, glatter Sandstein, Terrakotta, gelbe/weiße Terrakotta, Akazienholz und Wasser.  
**Generatorform:** Weite, sanfte Dünen mit trockenen Wadis; Stadt und Palast erhalten ebene Bauflächen.  
**Landmarken:** Stadttor, Markt, Palast, Oase, Ruinentempel.

## 23 – Skypia

**ID:** `skypia`  
**Region:** Himmelsmeer / Grand Line  
**Silhouette:** Schwebende Inselmasse über einem Wolkenmeer; mehrere kleinere Wolkeninseln bilden natürliche Nebenwege.

| Zone | Vorgesehene Gestaltung |
|---|---|
| Wolkenmeer | Wolkenplattformen und sichere Ankunftsinseln |
| Küstenrand | Steile Wolkenklippen und Wasserfall-/Wolkenfall-Effekte |
| Oberland | Helle Wiesen, ungewöhnliche Bäume und Wolkenpfade |
| Ruinen | Alte steinerne Anlagen und Säulen |
| Zentrum | Große goldene/helle Tempel- und Stadtzone |

**Materialpalette:** Weißer Beton, Quarz, Calcite, Glowstone, helle Wolle, Steinziegel, Blätter und ausgewählte Goldblöcke als Akzent.  
**Generatorform:** Getrennte schwebende Plattformen mit kontrollierten Brücken; Absturzschutz an Spawn und Hauptwegen.  
**Landmarken:** Ankunftsplattform, Wolkenbrücken, Ruinen, zentraler Tempel.

## 24 – Water 7

**ID:** `water_7`  
**Region:** Grand Line  
**Silhouette:** Dicht bebaute Hafenstadt mit mehreren Ebenen, Kanälen und Werftanlagen.

| Zone | Vorgesehene Gestaltung |
|---|---|
| Außenhafen | Docks, Schiffe als Blockmodelle und Lagerhäuser |
| Unterstadt | Kanäle, Brücken, Treppen und enge Gassen |
| Oberstadt | Mehrstöckige Häuser, Plätze und Geschäfte |
| Werft | Trockendocks, Kräne und Werkhallen |
| Hochpunkt | Großes markantes Verwaltungs-/Wahrzeichen |

**Materialpalette:** Ziegel, Steinziegel, Quarz, dunkle Eiche, Fichtenholz, Kupfer, Laternen und Wasser.  
**Generatorform:** Stadt zuerst als Straßen-/Kanalraster planen; Bauwerke danach in Parzellen platzieren. Wasserwege müssen verbunden und für Spieler passierbar sein.  
**Landmarken:** Hauptdock, Kanalplatz, Werft, große Brücke, Stadtzentrum.

## 25 – Sabaody Archipel

**ID:** `sabaody_archipelago`  
**Region:** Grand Line  
**Silhouette:** Mehrere große Mangroven-Inseln/Plattformen, verbunden durch Wurzeln, Stege und Brücken.

| Zone | Vorgesehene Gestaltung |
|---|---|
| Mangrovenwald | Riesige Baumstämme und breite Wurzelbereiche |
| Grove-Zentren | Nummerierte Plätze, Geschäfte und kleine Siedlungen |
| Küste | Schiffsstege und Blasen-/Korallendekoration |
| Verbindungswege | Wurzelbrücken, Holzstege und Seilbrücken |
| Unterwasserzugang | Markierter Übergang zu späterer Fischmenscheninsel-Anbindung |

**Materialpalette:** Eichen-/Mangroven-ähnliche Holzpalette aus Akazie und Eiche, Wurzeln, Blätter, Glas, Seelaternen, Korallenblöcke und prismarinefarbene Akzente.  
**Generatorform:** Archipel aus mehreren getrennten Groves; jeder Grove erhält eine eigene Bounding-Box und Nummer/ID.  
**Landmarken:** Hauptanleger, zentrale Mangroven, Grove-Plätze, Blasen-Dekoration und Unterwasserzugang.

## Test-Abnahmekriterien für Phase 1

Eine Insel gilt erst als spielbar umgesetzt, wenn:

1. Sie beim vorgesehenen Welt-/Archipel-Setup ohne manuelles Insel-Erstellen generiert wird.
2. Ihre Silhouette und Hauptbiome klar vom generischen Starterinsel-Profil unterscheidbar sind.
3. Spawnpunkt, Dock und Hauptwege sicher erreichbar sind.
4. Landmarken nicht ineinander oder in die Küste hinein platziert werden.
5. Ein Serverneustart die bereits generierte Insel nicht überschreibt oder dupliziert.
6. Java- und Bedrock-Spieler dieselben Bauwerke sehen und betreten können.
7. Die Generierung den Server nicht durch einen einzelnen langen synchronen Block-Loop einfriert.

## Reihenfolge der Implementierung

1. Datenmodell und Validierung der Profile.
2. Seed-basierte Geländeformen und Materialpaletten.
3. Küsten, Wasser und sichere Spawn-/Dockpunkte.
4. Landmarken und Straßen/Wege.
5. Persistenz, Chunk-/Abschnittsverarbeitung und Test auf dem Server.

Erst wenn diese fünf Inseln die Abnahmekriterien erfüllen, beginnt Phase 2 mit Fischmenscheninsel, Elbaf, Egghead, Baltigo und Kuraigana.
