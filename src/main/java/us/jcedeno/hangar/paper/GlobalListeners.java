package us.jcedeno.hangar.paper;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import fr.mrmicky.fastinv.ItemBuilder;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.objects.ProxyChangeInPlayersEvent;
import us.jcedeno.hangar.paper.scoreboard.ScoreboardManager;
import us.jcedeno.hangar.paper.tranciever.RapidInv;

public class GlobalListeners implements Listener {
    private Hangar instance;
    private ScoreboardManager scoreboardManager;
    private static @Getter String TRANSCEIVER_NAME = ChatColor.WHITE + "" + ChatColor.BOLD + "Transceiver";
    private static String ARENA_NAME = ChatColor.WHITE + "" + ChatColor.BOLD + "Arena";
    private static @Getter Location spawnLoc = Bukkit.getWorlds().get(0).getHighestBlockAt(0, 0).getLocation().add(0,
            2.0, 0);

    public GlobalListeners(Hangar instance) {
        this.instance = instance;
        this.scoreboardManager = instance.getScoreboardManager();
        Bukkit.getPluginManager().registerEvents(this, this.instance);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();

        var header = ChatColor.of("#A40A0A") + "" + ChatColor.BOLD + "\nNOOBSTERS\n";
        var footer = ChatColor.of("#4788d9") + "\nJoin Our Community!\n" + ChatColor.of("#2be49c")
                + "discord.noobsters.net\n" + ChatColor.AQUA + "twitter.com/NoobstersMC\n " + ChatColor.GOLD
                + "noobsters.buycraft.net\n";

        player.setPlayerListHeaderFooter(header, footer);
        player.teleportAsync(spawnLoc).thenAccept(a -> {
            player.sendMessage(ChatColor.BLUE + "Discord! discord.noobsters.net\n" + ChatColor.AQUA
                    + "Twitter! twitter.com/NoobstersMC\n" + ChatColor.GOLD + "Donations! noobsters.buycraft.net");
        });
        player.setFoodLevel(20);
        player.setSaturation(20F);
        player.setGameMode(GameMode.ADVENTURE);
        e.setJoinMessage("");
        giveTransciever(player);
        // Send the player a scoreboard
        scoreboardManager.sendInitialBoard(player);
    }

    @EventHandler
    public void onPlayersChange(ProxyChangeInPlayersEvent e) {
        instance.getScoreboardManager().getBoards().values().parallelStream().forEach(boards -> {
            boards.updateLine(7, " " + e.getPlayers());
        });
    }

    @EventHandler
    public void onShieldBreak(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            var victim = (Player) e.getEntity();
            var player = (Player) e.getDamager();
            if (victim.isBlocking() && isAxe(player.getInventory().getItemInMainHand())) {
                player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
            }

        }
    }

    private boolean isAxe(ItemStack e) {
        return e != null && e.getType().toString().contains("_AXE");
    }

    @EventHandler
    public void cleanScoreboard(PlayerQuitEvent e) {
        // Remove old scoreboard if present.
        scoreboardManager.getBoards().remove(e.getPlayer().getUniqueId());

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onArrow(EntityDamageByEntityEvent e) {
        var damager = e.getDamager();
        if (damager instanceof Trident || damager instanceof Egg || damager instanceof FishHook
                || damager instanceof Snowball || !(damager instanceof Projectile))
            return;

        if (!(((Projectile) e.getDamager()).getShooter() instanceof Player))
            return;
        if (!(e.getEntity() instanceof Player))
            return;

        Player shooter = ((Player) ((Projectile) e.getDamager()).getShooter());

        var data = instance.getArena().getArenaUsers().get(shooter.getUniqueId());
        if (data != null) {
            data.setLastDamageTime(System.currentTimeMillis());
        }

        Player p = (Player) e.getEntity();

        if (p.getHealth() - e.getFinalDamage() <= 0.0D || p.isBlocking())
            return;

        if (shooter == p)
            return;

        shooter.sendMessage(ChatColor.GOLD + "🏹 " + p.getDisplayName() + ChatColor.GRAY + " is at " + ChatColor.WHITE
                + (((int) (p.getHealth() - e.getFinalDamage())) / 2.0D) + ChatColor.DARK_RED + "❤");

    }

    /*
     * Transciever code starts
     */

    private boolean isRapidInventory(Inventory inv) {
        return inv != null && inv.getHolder() instanceof RapidInv;
    }

    @EventHandler
    public void onTransceiverOpen(PlayerInteractEvent e) {
        //Don't allow to interact if they already are viewing a rapidInv
        var top = e.getPlayer().getOpenInventory().getTopInventory();
        var bottom = e.getPlayer().getOpenInventory().getBottomInventory();
        if (isRapidInventory(top) || isRapidInventory(bottom)) {
            return;
        }
        //Handle items.
        if (e.getAction() != Action.PHYSICAL && e.getMaterial() == Material.NETHER_STAR) {
            instance.getCommunicatorManager().getServerGui().open(e.getPlayer());
        } else if (e.getMaterial() == Material.NETHERITE_SWORD && !instance.getArena().isInArena(e.getPlayer())) {
            e.getPlayer().performCommand("arena");
        }
    }

    @EventHandler
    public void onTransceiverDrop(PlayerDropItemEvent e) {
        final var item = e.getItemDrop().getItemStack();
        if (isTransceiver(item)) {
            e.setCancelled(true);
        }

    }

    private boolean humanInArena(HumanEntity entity) {
        return instance.getArena().isInArena((Player) entity);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (!humanInArena(e.getWhoClicked()) && !e.getWhoClicked().hasPermission("lobby.edit")) {
            e.setCancelled(true);
            return;
        }
        if (e.getOldCursor() != null && isTransceiver(e.getOldCursor()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onTranscieverMove(InventoryClickEvent e) {
        if (!humanInArena(e.getWhoClicked()) && !e.getWhoClicked().hasPermission("lobby.edit")) {
            e.setCancelled(true);
            return;
        }
        var c = e.getHotbarButton();
        var f = e.getClickedInventory();

        if (f != null) {
            if ((f.getType() != InventoryType.PLAYER || e.getSlot() > 8) && isTransceiver(e.getCursor())) {
                e.setCancelled(true);
                e.getWhoClicked().sendMessage(ChatColor.RED + "You must keep the tranceiver on your hotbar.");
            }
            if (c > -1) {
                var item = e.getWhoClicked().getInventory().getContents()[c];
                if (item != null && isTransceiver(item)) {
                    e.setCancelled(true);
                    e.getWhoClicked().sendMessage(ChatColor.RED + "You must keep the tranceiver on your hotbar.");
                } else {
                    e.setCancelled(false);
                }

            }
        }

    }

    @EventHandler
    public void onPlayerSwap(PlayerSwapHandItemsEvent e) {
        if (!humanInArena(e.getPlayer()) && !e.getPlayer().hasPermission("lobby.edit")) {
            e.setCancelled(true);
            return;
        }

        var item = e.getMainHandItem();
        var offItem = e.getOffHandItem();
        if ((item != null && isTransceiver(item)) || (offItem != null && isTransceiver(offItem))) {
            e.getPlayer().sendMessage(ChatColor.RED + "You must keep the tranceiver on your hotbar.");
            e.setCancelled(true);
        }

    }

    private boolean isTransceiver(final ItemStack stack) {
        return stack.getType() == Material.NETHER_STAR && stack.hasItemMeta()
                && stack.getItemMeta().getDisplayName().equalsIgnoreCase(TRANSCEIVER_NAME);
    }

    public static void giveTransciever(final Player player) {
        // Obtain the inventory
        final var inv = player.getInventory();
        // Cleanup the inventory
        inv.clear();
        inv.setArmorContents(null);
        player.setLevel(0);
        player.setExp(0);
        player.setHealth(20);
        player.getActivePotionEffects().forEach(a -> player.removePotionEffect(a.getType()));
        // Add transceiver to slot 4 (5) on the hotbar
        inv.setItem(4, new ItemBuilder(Material.NETHER_STAR).name(TRANSCEIVER_NAME).enchant(Enchantment.VANISHING_CURSE)
                .flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES).build());
        inv.setItem(0, new ItemBuilder(Material.NETHERITE_SWORD).name(ARENA_NAME).enchant(Enchantment.VANISHING_CURSE)
                .flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES).build());

    }
    /*
     * Transciever code ends
     */

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        if (Bukkit.getOnlinePlayers().size() >= instance.getMaxSlots() && !e.getPlayer().hasPermission("reserved.slot"))
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.translateAlternateColorCodes('&',
                    "&fServer is full! \n &aGet your rank at &6noobsters.buycraft.net"));

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!e.getPlayer().hasPermission("lobby.edit")) {
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!e.getPlayer().hasPermission("lobby.edit"))
            e.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage("");
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
    public void onEntityDamage(EntityDamageByEntityEvent e) {
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

    @EventHandler(priority = EventPriority.LOW)
    public void nerfCriticalDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity().getType() != EntityType.PLAYER || e.getDamager().getType() != EntityType.PLAYER)
            return;
        var damager = (Player) e.getDamager();
        if (isCritical(damager)) {
            final var damage = e.getDamage();
            final var backToBaseDamage = damage / 1.5;
            final var damageDifferential = damage - backToBaseDamage;
            if (damageDifferential > 2)
                e.setDamage(backToBaseDamage + 4);

        }
    }

    @SuppressWarnings("all")
    private boolean isCritical(Player player) {
        return player.getFallDistance() > 0.0F && !player.isOnGround() && !player.isInsideVehicle()
                && !player.hasPotionEffect(PotionEffectType.BLINDNESS)
                && player.getLocation().getBlock().getType() != Material.LADDER
                && player.getLocation().getBlock().getType() != Material.VINE;
    }

}
