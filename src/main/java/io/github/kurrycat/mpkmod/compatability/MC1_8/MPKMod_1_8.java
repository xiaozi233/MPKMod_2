package io.github.kurrycat.mpkmod.compatability.MC1_8;

import io.github.kurrycat.mpkmod.compatability.API;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

@Mod(
        modid = API.MODID,
        version = API.VERSION,
        name = API.NAME,
        acceptedMinecraftVersions = "*"//,
        //updateJSON = "https://raw.githubusercontent.com/kurrycat2004/MpkMod/main/update.json",
        //guiFactory = MPKMod.GUI_FACTORY
)
public class MPKMod_1_8 {
    //public static final String GUI_FACTORY = "io.github.kurrycat.mpkmod.config.GuiFactory";

    public static Map<String, KeyBinding> keyBindingMap = new HashMap<>();

    @EventHandler
    public void init(FMLInitializationEvent event) {
        API.preInit();

        API.guiScreenMap.forEach((id, guiScreen) -> {
            if (guiScreen.shouldCreateKeyBind()) {
                KeyBinding keyBinding = new KeyBinding(
                        API.MODID + ".key." + id + ".desc",
                        Keyboard.KEY_NONE,
                        API.KEYBINDING_CATEGORY
                );
                keyBindingMap.put(id, keyBinding);
                ClientRegistry.registerKeyBinding(keyBinding);
            }
        });

        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Registering compatibility functions...");
        API.registerFunctionHolder(new FunctionCompatibility());
        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Done");

        MinecraftForge.EVENT_BUS.register(new EventListener());
        MinecraftForge.EVENT_BUS.register(this);

        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Registering Keybindings...");
        for (KeyBinding k : Minecraft.getMinecraft().gameSettings.keyBindings) {
            new io.github.kurrycat.mpkmod.compatability.MCClasses.KeyBinding(
                    () -> GameSettings.getKeyDisplayString(k.getKeyCode()),
                    k.getKeyDescription(),
                    k::isKeyDown
            );
        }
        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Done");

        API.init(Minecraft.getSessionInfo().get("X-Minecraft-Version"));
    }

    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent e) {
        API.Events.onLoadComplete();
    }
}
