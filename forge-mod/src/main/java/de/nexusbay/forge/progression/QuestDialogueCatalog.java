package de.nexusbay.forge.progression;

import java.util.Map;

/** Island-specific quest giver identities and opening dialogue. */
public final class QuestDialogueCatalog {
    public record Dialogue(String name, String greeting) {}
    private static final Map<String, Dialogue> DIALOGUES = Map.ofEntries(
        Map.entry("FooshaVillage", new Dialogue("Makino's Bote", "Das Dorf braucht Hilfe. Kannst du unsere Küste schützen?")),
        Map.entry("ShellsTown", new Dialogue("Marine-Rekrut", "Die Patrouillen melden Ärger. Wir brauchen Verstärkung!")),
        Map.entry("OrangeTown", new Dialogue("Bürgermeister Boodle", "Unsere Stadt ist in Gefahr. Hilf uns, die Straßen zu sichern.")),
        Map.entry("SyrupVillage", new Dialogue("Dorfwache Kaya", "Im Wald regt sich etwas. Bitte halte die Wege sicher.")),
        Map.entry("Baratie", new Dialogue("Baratie-Koch", "Räuber bedrohen unsere Gäste. Das lassen wir nicht zu!")),
        Map.entry("ArlongPark", new Dialogue("Freiheitskämpfer", "Arlongs Schrecken muss enden. Das Dorf zählt auf dich.")),
        Map.entry("Loguetown", new Dialogue("Hafenoffizier", "Gesetzlose treiben sich herum. Sorge für Ordnung.")),
        Map.entry("ReverseMountain", new Dialogue("Wächter der Strömung", "Der Aufstieg ist gefährlich. Halte die Passage frei.")),
        Map.entry("WhiskeyPeak", new Dialogue("Kopfgeldjäger", "Auf dieser Insel lauert hinter jedem Lächeln eine Gefahr.")),
        Map.entry("LittleGarden", new Dialogue("Forschungsleiter", "Dieser Urwald ist gnadenlos. Beweise, dass du hier bestehen kannst.")),
        Map.entry("DrumIsland", new Dialogue("Königreichswache", "Der Schnee verbirgt Gefahren. Schütze die Versorgung.")),
        Map.entry("Alabasta", new Dialogue("Königlicher Bote", "Das Wüstenreich steht am Rand des Chaos. Wir brauchen dich.")),
        Map.entry("Jaya", new Dialogue("Hafen-Schläger", "In Mock Town zählt nur Stärke. Zeig, was du kannst.")),
        Map.entry("Skypiea", new Dialogue("Himmelsritter", "Die Wege über den Wolken sind bedroht. Verteidige Skypiea!")),
        Map.entry("Water7", new Dialogue("Schiffsbauer", "An den Docks gibt es Ärger. Unsere Werften brauchen Schutz.")),
        Map.entry("EniesLobby", new Dialogue("Torwache", "Die Festung steht unter Druck. Kein Eindringling darf passieren.")),
        Map.entry("ThrillerBark", new Dialogue("Nebelwächter", "Bleib wachsam. Im Nebel bewegt sich etwas Unheimliches.")),
        Map.entry("Sabaody", new Dialogue("Grove-Aufseher", "Das Archipel ist außer Kontrolle. Sichere die Groves.")),
        Map.entry("AmazonLily", new Dialogue("Kriegerinnen-Wache", "Fremde bedrohen unsere Heimat. Vertreibe die Eindringlinge.")),
        Map.entry("ImpelDown", new Dialogue("Gefängniswärter", "In den Blöcken herrscht Unruhe. Stelle die Ordnung wieder her.")),
        Map.entry("Marineford", new Dialogue("Marine-Kommandant", "Die Front hält kaum stand. Wir brauchen jede helfende Hand.")),
        Map.entry("FishManIsland", new Dialogue("Palastwache", "Gefahren steigen aus der Tiefe. Beschütze das Königreich.")),
        Map.entry("PunkHazard", new Dialogue("Labor-Überlebender", "Feuer und Eis machen die Insel tödlich. Pass auf dich auf.")),
        Map.entry("Dressrosa", new Dialogue("Widerstandskämpfer", "Die Stadt braucht einen Helden. Befreie ihre Viertel.")),
        Map.entry("Zou", new Dialogue("Mink-Wächter", "Unsere Heimat ist in Gefahr. Verteidige das Land auf dem Elefanten.")),
        Map.entry("WholeCakeIsland", new Dialogue("Konditor-Wächter", "Süße Fassaden täuschen. Halte die Gefahren von uns fern.")),
        Map.entry("WanoKuni", new Dialogue("Samurai-Späher", "Das Land wartet auf Befreiung. Kämpfe ehrenhaft!")),
        Map.entry("Onigashima", new Dialogue("Rebell des Oniwabanshu", "Onigashima ist ein Schlachtfeld. Nur die Stärksten überleben."))
    );
    public static Dialogue find(String islandId) {
        return DIALOGUES.getOrDefault(islandId, new Dialogue("Questgeber", "Die Insel braucht deine Hilfe."));
    }
    private QuestDialogueCatalog() {}
}
