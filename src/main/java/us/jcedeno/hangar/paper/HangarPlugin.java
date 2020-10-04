package us.jcedeno.hangar.paper;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class HangarPlugin extends JavaPlugin{

    @Override
    public void onEnable() {
        Bukkit.broadcastMessage("Hangar is online");

    }

    @Override
    public void onDisable() {
        
    }
    
}
