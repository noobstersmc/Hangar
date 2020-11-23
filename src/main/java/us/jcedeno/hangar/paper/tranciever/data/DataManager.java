package us.jcedeno.hangar.paper.tranciever.data;

import org.bukkit.Bukkit;

import us.jcedeno.hangar.paper.tranciever.RapidInv;
import us.jcedeno.hangar.paper.tranciever.guis.browser.BrowserWindow;

public class DataManager {

    public void sendUpdate(){
        
        Bukkit.getOnlinePlayers().forEach(all -> {
            var inv = all.getOpenInventory().getTopInventory();
            if (inv.getHolder() instanceof RapidInv) {
                Bukkit.broadcastMessage("Rapid window");
                if (inv.getHolder() instanceof BrowserWindow) {

                    Bukkit.broadcastMessage("Browser window");
                    var browser = (BrowserWindow) inv.getHolder();
                   // browser.update(hashSet);
                }
            }
        });
    }
    
}
