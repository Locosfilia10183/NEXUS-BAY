package de.nexusbay.forge.progression;
import java.util.List;
import java.util.Map;
/** Skill thresholds mirrored from the fruit mastery data resources. */
public final class FruitSkillCatalog {
 public record Skill(String id,String name,int requiredLevel,int xpCost,int cooldownTicks,String animationId) {}
 private static final Map<String,List<Skill>> SKILLS=Map.of(
  "gomu_gomu",List.of(new Skill("pistol","Gum-Gum-Pistole",1,0,40,"gomu_pistol"),new Skill("rocket","Gum-Gum-Rakete",5,250,100,"gomu_rocket"),new Skill("bazooka","Gum-Gum-Bazooka",10,700,160,"gomu_bazooka"),new Skill("gatling","Gum-Gum-Gatling",20,1800,240,"gomu_gatling"),new Skill("gear_second","Gear Second",35,5000,1200,"gear_second")),
  "goro_goro",List.of(new Skill("lightning_bolt","Blitzschlag",1,0,50,"goro_lightning_bolt"),new Skill("thunder_tempo","Donnersturm",10,900,220,"goro_thunder_tempo"),new Skill("lightning_travel","Blitzbewegung",22,2500,300,"goro_lightning_travel"),new Skill("raigo","Raigo",45,8000,1200,"goro_raigo")),
  "mera_mera",List.of(new Skill("fire_fist","Feuerfaust",1,0,60,"mera_fire_fist"),new Skill("fire_pillar","Flammensäule",8,600,180,"mera_fire_pillar"),new Skill("firefly","Feuerfliegen",18,1800,240,"mera_firefly"),new Skill("flame_emperor","Flammenkaiser",40,6500,900,"mera_flame_emperor"))
 );
 private FruitSkillCatalog(){}
 public static List<Skill> forFruit(String id){return SKILLS.getOrDefault(id,List.of());}
 public static Skill next(String id,int level){return forFruit(id).stream().filter(s->s.requiredLevel()>level).findFirst().orElse(null);}
 public static List<Skill> unlocked(String id,int level){return forFruit(id).stream().filter(s->s.requiredLevel()<=level).toList();}
}
