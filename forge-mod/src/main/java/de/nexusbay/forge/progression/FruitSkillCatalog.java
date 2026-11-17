package de.nexusbay.forge.progression;
import java.util.List;
import java.util.Map;

/** Unlock progression for every fruit currently available in the case pool. */
public final class FruitSkillCatalog {
 public record Skill(String id,String name,int requiredLevel,int xpCost,int cooldownTicks,String animationId) {}
 private static List<Skill> pair(String first,String second,int level,int cost,int cooldown,String prefix){
  return List.of(new Skill(first,first,1,0,40,prefix+"_basic"),new Skill(second,second,level,cost,cooldown,prefix+"_advanced"));
 }
 private static final Map<String,List<Skill>> SKILLS=Map.ofEntries(
  Map.entry("gomu_gomu",List.of(new Skill("pistol","Gum-Gum-Pistole",1,0,40,"gomu_pistol"),new Skill("rocket","Gum-Gum-Rakete",5,250,100,"gomu_rocket"),new Skill("bazooka","Gum-Gum-Bazooka",10,700,160,"gomu_bazooka"),new Skill("gatling","Gum-Gum-Gatling",20,1800,240,"gomu_gatling"),new Skill("gear_second","Gear Second",35,5000,1200,"gear_second"),new Skill("gear_third","Gear Third",50,9000,1400,"gear_third"))),
  Map.entry("goro_goro",List.of(new Skill("lightning_bolt","Blitzschlag",1,0,50,"goro_lightning_bolt"),new Skill("thunder_tempo","Donnersturm",10,900,220,"goro_thunder_tempo"),new Skill("lightning_travel","Blitzbewegung",22,2500,300,"goro_lightning_travel"),new Skill("raigo","Raigo",45,8000,1200,"goro_raigo"))),
  Map.entry("mera_mera",List.of(new Skill("fire_fist","Feuerfaust",1,0,60,"mera_fire_fist"),new Skill("fire_pillar","Flammensäule",8,600,180,"mera_fire_pillar"),new Skill("firefly","Feuerfliegen",18,1800,240,"mera_firefly"),new Skill("flame_emperor","Flammenkaiser",40,6500,900,"mera_flame_emperor"))),
  Map.entry("supa_supa",pair("Klingenhaut","Klingensturm",8,500,100,"supa")),
  Map.entry("bomu_bomu",pair("Explosionsfaust","Bombenfeld",12,800,140,"bomu")),
  Map.entry("kilo_kilo",pair("Leichtgewicht","Schwergewicht",10,650,120,"kilo")),
  Map.entry("bane_bane",pair("Sprungfeder","Federkanone",12,800,140,"bane")),
  Map.entry("doru_doru",pair("Wachsfaust","Wachsfessel",10,650,120,"doru")),
  Map.entry("sube_sube",pair("Gleittritt","Gleitsturm",10,650,120,"sube")),
  Map.entry("ori_ori",pair("Fesselgriff","Eisenkäfig",12,800,140,"ori")),
  Map.entry("bara_bara",pair("Trennhieb","Körperregen",12,800,140,"bara")),
  Map.entry("yomi_yomi",pair("Seelenschnitt","Frostseele",18,1500,180,"yomi")),
  Map.entry("ito_ito",pair("Fadenpeitsche","Fadenkäfig",20,1800,220,"ito")),
  Map.entry("ope_ope",pair("ROOM","Takt",25,2500,260,"ope")),
  Map.entry("magu_magu",pair("Magmaschlag","Magmaeruption",25,2500,260,"magu")),
  Map.entry("hie_hie",pair("Eislanze","Eiszeit",25,2500,260,"hie")),
  Map.entry("pika_pika",pair("Lichttritt","Yasakani",30,3200,300,"pika")),
  Map.entry("soru_soru",pair("Seelenraub","Homies Sturm",30,3200,300,"soru")),
  Map.entry("uo_uo_seiryu",List.of(new Skill("dragon_roar","Drachenbrüllen",1,0,100,"seiryu_roar"),new Skill("flame_cloud","Flammenwolke",15,1200,220,"seiryu_cloud"),new Skill("azure_dragon","Himmelsdrache",40,7000,1000,"seiryu_dragon")))
 );
 private FruitSkillCatalog(){}
 public static List<Skill> forFruit(String id){return SKILLS.getOrDefault(id,List.of());}
 public static Skill next(String id,int level){return forFruit(id).stream().filter(s->s.requiredLevel()>level).findFirst().orElse(null);}
 public static List<Skill> unlocked(String id,int level){return forFruit(id).stream().filter(s->s.requiredLevel()<=level).toList();}
}
