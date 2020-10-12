package us.jcedeno.hangar.paper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.ItemStack;

import fr.mrmicky.fastinv.ItemBuilder;
import net.md_5.bungee.api.ChatColor;

public class GlobalListeners implements Listener {
    private Hangar instance;
    private static String TRANSCEIVER_NAME = ChatColor.WHITE + "" + ChatColor.BOLD + "Transceiver";
    private Location spawnLoc = Bukkit.getWorlds().get(0).getHighestBlockAt(0, 0).getLocation().add(0, 2.0, 0);

    public GlobalListeners(Hangar instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();
        player.sendMessage(ChatColor.BLUE + "Discord! discord.noobsters.net\n" + ChatColor.AQUA
                + "Twitter! twitter.com/NoobstersMC\n" + ChatColor.GOLD + "Donations! noobsters.buycraft.net");
        var header = ChatColor.of("#A40A0A") + "" + ChatColor.BOLD + "\nNOOBSTERS\n";
        var footer = ChatColor.of("#4788d9") + "\nJoin Our Community!\n" + ChatColor.of("#2be49c")
                + "discord.noobsters.net\n" + ChatColor.AQUA + "twitter.com/NoobstersMC\n " + ChatColor.GOLD
                + "noobsters.buycraft.net\n";

        player.setPlayerListHeaderFooter(header, footer);
        player.teleport(spawnLoc);
        player.setFoodLevel(20);
        player.setSaturation(20F);
        e.setJoinMessage("");
        giveTransciever(player);
    }

    /*
     * Transciever code starts
     */

    @EventHandler
    public void onTransceiverOpen(PlayerInteractEvent e) {
        if (e.getAction() != Action.PHYSICAL && e.getMaterial() == Material.NETHER_STAR) {
            instance.getServerGui().open(e.getPlayer());
        }
    }

    @EventHandler
    public void onTransceiverDrop(PlayerDropItemEvent e) {
        final var item = e.getItemDrop().getItemStack();
        if (isTransceiver(item)) {
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onTranscieverMove(InventoryClickEvent e) {
        if ((e.getCursor() != null && isTransceiver(e.getCursor()))
                || (e.getCurrentItem() != null && isTransceiver(e.getCurrentItem())))
            e.setCancelled(true);

        if (e.getClick() == ClickType.NUMBER_KEY) {
            var button = e.getHotbarButton();
            var hotbarItem = e.getClickedInventory().getItem(button);
            if (hotbarItem != null && isTransceiver(hotbarItem)) {
                e.setCancelled(true);
            }
        }
        if (e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER) {
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerSwap(PlayerSwapHandItemsEvent e) {
        var item = e.getMainHandItem();
        var offItem = e.getOffHandItem();
        if ((item != null && isTransceiver(item)) || (offItem != null && isTransceiver(offItem)))
            e.setCancelled(true);

    }

    private boolean isTransceiver(final ItemStack stack) {
        return stack.getType() == Material.NETHER_STAR && stack.hasItemMeta()
                && stack.getItemMeta().getDisplayName().equalsIgnoreCase(TRANSCEIVER_NAME);
    }

    private void giveTransciever(final Player player) {
        // Obtain the inventory
        final var inv = player.getInventory();
        // Cleanup the inventory
        inv.clear();
        inv.setArmorContents(null);
        // Add transceiver to slot 4 (5) on the hotbar
        inv.setItem(4, new ItemBuilder(Material.NETHER_STAR).name(TRANSCEIVER_NAME).build());
    }
    /*
     * Transciever code ends
     */

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        if (Bukkit.getOnlinePlayers().size() >= 100 && !e.getPlayer().hasPermission("reserved.slot"))
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.translateAlternateColorCodes('&',
                    "&fServer is full! \n &aGet your rank at noobstersuhc.buycraft.net"));
    }

    

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage("");
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager().getType() == EntityType.PLAYER && e.getEntity().getType() == EntityType.PLAYER)
            e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntityType() == EntityType.PLAYER && e.getCause() == EntityDamageEvent.DamageCause.FALL)
            e.setCancelled(true);

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
        else if (!e.getPlayer().hasPermission("lobby.edit"))
            e.setCancelled(true);
    }

    @EventHandler
    public void onDamageMap(EntityDamageEvent e) {
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
