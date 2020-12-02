package us.jcedeno.hangar.paper;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

public class Decoration implements Listener {
    private Hangar instance;
    private final Random random = new Random();

    public Decoration(Hangar instance) {
        this.instance = instance;
        Bukkit.getPluginManager().registerEvents(this, this.instance);
    }

    @EventHandler
    public void onShulkerDamage(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof ShulkerBullet)
            e.setDamage(0.00001);
        if(e.getEntity() instanceof Shulker) e.setCancelled(true);
        
        
    }

    @EventHandler
    public void onShulkerTp(EntityTeleportEvent e){
        if(e.getEntity() instanceof Shulker) e.setCancelled(true);

        
    }

    @EventHandler
    public void cancelLeavesDecay(LeavesDecayEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    public void onOpenShulker(InventoryOpenEvent e){
        if(e.getInventory().getType() == InventoryType.SHULKER_BOX) e.setCancelled(true);

    }

    @EventHandler
    public void shulkerBulletSpawn(EntitySpawnEvent e){
        if(e.getEntity() instanceof ShulkerBullet){
            if(random.nextBoolean()) e.setCancelled(true);
        }
    }
    /*
    @EventHandler
    public void onOpenTrapdoor(PlayerInteractEvent e){
        if (!e.getPlayer().hasPermission("lobby.edit") && 
            e.getClickedBlock().getType().toString().contains("TRAPDOOR")) {
            e.setCancelled(true);
        }
    }*/


}
