package de.nexusbay.forge.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.nexusbay.forge.NexusBayForge;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** G activates the currently highest unlocked fruit skill. */
@Mod.EventBusSubscriber(modid=NexusBayForge.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class FruitSkillKeybind {
 public static final KeyMapping USE_SKILL=new KeyMapping("key.nexusbay.use_fruit_skill",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_G,"key.categories.nexusbay");
 public static final KeyMapping CYCLE_SKILL=new KeyMapping("key.nexusbay.cycle_fruit_skill",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_H,"key.categories.nexusbay");
 private FruitSkillKeybind(){}
 @SubscribeEvent public static void register(RegisterKeyMappingsEvent event){event.register(USE_SKILL);event.register(CYCLE_SKILL);}
}
