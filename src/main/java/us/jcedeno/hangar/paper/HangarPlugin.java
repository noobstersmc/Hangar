package us.jcedeno.hangar.paper;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class HangarPlugin extends JavaPlugin {
    // GUI tutorial: https://github.com/MrMicky-FR/FastInv
    // Scoreboard Tutorial: https://github.com/MrMicky-FR/FastBoard
    // Commands Tutorial: https://github.com/aikar/commands/wiki/Using-ACF
    @Override
    public void onEnable() {
        Bukkit.broadcastMessage("Hangar is online");

    }

    @Override
    public void onDisable() {

    }

}
