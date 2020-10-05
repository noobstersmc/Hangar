package us.jcedeno.hangar.paper;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;

public class GlobalListeners implements Listener {
    Hangar instance;
    private Location spawnLoc = Bukkit.getWorlds().get(0).getBlockAt(416, 48, 9).getLocation();

    public GlobalListeners(Hangar instance){
        this.instance = instance;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().sendMessage(ChatColor.BLUE + "Discord! discord.noobsters.net\n" + ChatColor.AQUA
                + "Twitter! twitter.com/NoobstersMC\n" + ChatColor.GOLD + "Donations! noobsters.buycraft.net");
        var header = ChatColor.of("#A40A0A") + "" + ChatColor.BOLD + "\nNOOBSTERS\n";
        var footer = ChatColor.of("#4788d9") + "\nJoin Our Community!\n" + ChatColor.of("#2be49c")
                + "discord.noobsters.net\n" + ChatColor.AQUA
                + "twitter.com/NoobstersMC\n";

        e.getPlayer().setPlayerListHeaderFooter(header, footer);
        e.getPlayer().teleport(spawnLoc);
        e.setJoinMessage("");
    }


    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        if (Bukkit.getOnlinePlayers().size() >= 100 && !e.getPlayer().hasPermission("reserved.slot"))
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.translateAlternateColorCodes('&',
                    "&fServer is full! \n &aGet your rank at noobstersuhc.buycraft.net"));
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        e.setQuitMessage("");
    }

    public void onDamage(EntityDamageByEntityEvent e){
        if(e.getEntity().getType() == EntityType.PLAYER){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!e.getPlayer().hasPermission("lobby.edit"))
            e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!e.getPlayer().hasPermission("lobby.edit"))
            e.setCancelled(true);

    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent e) {
        e.setFoodLevel(20);
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerLectern(PlayerTakeLecternBookEvent e) {
        if (!e.getPlayer().hasPermission("lobby.edit"))
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntity(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() == null || e.getRightClicked().getType() != EntityType.ARMOR_STAND)
            return;
        else if(!e.getPlayer().hasPermission("lobby.edit"))
            e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        switch (e.getEntityType()) {
            case ARMOR_STAND:
            case ITEM_FRAME:
            case PAINTING: {
                if (e.getCause() == DamageCause.ENTITY_EXPLOSION || e.getCause() == DamageCause.BLOCK_EXPLOSION
                        || e.getCause() == DamageCause.PROJECTILE) {
                    e.setCancelled(true);
                }
            }
            default:
                break;
        }

    }

    @EventHandler
    public void onDamageByPlayer(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && !e.getDamager().hasPermission("lobby.edit")) {
            switch (e.getEntity().getType()) {
                case ARMOR_STAND:
                case PAINTING:
                case ITEM_FRAME: {
                    e.setCancelled(true);
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }



}
