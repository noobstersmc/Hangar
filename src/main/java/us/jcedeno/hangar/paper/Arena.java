package us.jcedeno.hangar.paper;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Criterias;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Name;
import co.aikar.commands.annotation.Subcommand;
import fr.mrmicky.fastinv.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.objects.CoordinatePair;

/**
 * InnerArena
 */
@CommandAlias("arena|practice|a|p|ffa")
public class Arena extends BaseCommand implements Listener {
    // Objects to control the data for random teleport
    private @Getter @Setter CoordinatePair centerCoordinate = CoordinatePair.of(-160, 0, -300);
    private @Getter @Setter int radius = 100;
    // An instance of the plugin
    private @Getter int arenaLimits = 30;
    private Hangar instance;
    // Loading cacche to self expire players
    private @Getter LoadingCache<UUID, Integer> cache = Caffeine.newBuilder().scheduler(Scheduler.systemScheduler())
            .removalListener((UUID key, Integer value, RemovalCause cause) -> {
                if (cause == RemovalCause.EXPIRED) {
                    var optionalPlayer = Bukkit.getOnlinePlayers().stream()
                            .filter(a -> a.getUniqueId().getMostSignificantBits() == key.getMostSignificantBits())
                            .findFirst();
                    if (optionalPlayer.isPresent()) {
                        var player = optionalPlayer.get();
                        player.sendMessage(
                                ChatColor.RED + "You have been kicked out of the arena for being idle for too long.");
                        System.out.println(cause);
                        Bukkit.getScheduler().runTask(instance, () -> leaveArena(player));
                    }
                }
            }).expireAfterWrite(5, TimeUnit.MINUTES).build(entry -> null);
    // Auto Lapiz
    private final ItemStack lapis = new ItemBuilder(Material.LAPIS_LAZULI).amount(64).build();
    private final Random random = new Random();
    // Local Scoreboard for hearts
    private Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

    public Arena(Hangar instance) {
        this.instance = instance;
        //Register listener and command.
        this.instance.getCommandManager().registerCommand(this);
        Bukkit.getPluginManager().registerEvents(this, instance);
        //Register health objectives for arena.
        scoreboard.registerNewObjective("health2", Criterias.HEALTH, ChatColor.DARK_RED + "❤", RenderType.INTEGER)
                .setDisplaySlot(DisplaySlot.PLAYER_LIST);
        scoreboard.registerNewObjective("health", Criterias.HEALTH, ChatColor.DARK_RED + "❤", RenderType.HEARTS)
                .setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    // Commands
    @Default
    public void onPratice(Player player) {
        if (cache.asMap().size() >= this.arenaLimits && !player.hasPermission("reserved.slot")) {
            player.sendMessage(
                    "Arena FFA is full! \n Get your rank at " + ChatColor.GREEN + "noobstersuhc.buycraft.net");
            return;
        }
        if (isInArena(player)) {
            leaveArena(player);
        } else {
            player.sendActionBar("Joining the arena...");
            player.setScoreboard(scoreboard);
            hideOthers(player);
            giveKit(player);
            teleportPlayer(player);
            cache.put(player.getUniqueId(), 0);
        }

    }

    private void leaveArena(final Player player) {
        cache.invalidate(player.getUniqueId());
        player.sendActionBar("Leaving the arena...");
        GlobalListeners.giveTransciever(player);
        player.teleport(GlobalListeners.getSpawnLoc());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        showEveryone(player);

    }

    @Subcommand("slots")
    @CommandPermission("uhc.admin")
    public void onChangeSlots(CommandSender sender, @Name("new-slots") Integer arg) {
        this.arenaLimits = arg.intValue();
        sender.sendMessage("Arena slots set to " + this.arenaLimits);
    }

    private boolean isInArena(Player player) {
        return cache.getIfPresent(player.getUniqueId()) != null;
    }

    @SuppressWarnings("all")
    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent e) {
        if (!isInArena(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    // ON JOIN arena oculta los de afuera y se desoculta para todos
    // ON LEFT se oculta para arena se desocultan todos
    // on join server se oculta para los de la arena

    private void hideOthers(Player player) {
        Bukkit.getOnlinePlayers().stream().filter(a -> a != player).filter(all -> !isInArena(all))
                .forEach(hide -> player.hidePlayer(instance, hide));

        Bukkit.getOnlinePlayers().forEach(a -> {
            a.showPlayer(instance, player);
        });
    }

    private void showEveryone(Player player) {
        Bukkit.getOnlinePlayers().forEach(all -> {
            if (isInArena(all)) {
                all.hidePlayer(instance, player);
            } else {
                player.showPlayer(instance, all);
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getOnlinePlayers().stream().filter(all -> isInArena(all))
                .forEach(toHideFrom -> toHideFrom.hidePlayer(instance, e.getPlayer()));
    }

    /*
     * Cancel Fall Damage, Entity Damage, and Projectile Damage to player's that are
     * NOT in the arena.
     */
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntityType() == EntityType.PLAYER
                && (e.getCause() == EntityDamageEvent.DamageCause.FALL
                        || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)
                || e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            var player = (Player) e.getEntity();
            if (!isInArena(player)) {
                e.setCancelled(true);
            }

        }

    }

    @EventHandler
    public void onPlayerDamageEvent(EntityDamageByEntityEvent e) {
        var damage = e.getDamager();

        if (damage instanceof Player) {
            var player = (Player) damage;
            if (!isInArena(player)) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (isInArena(e.getPlayer())) {
            giveKit(e.getPlayer());
            e.setRespawnLocation(getRandomLocation(e.getPlayer().getWorld()));
        } else {
            GlobalListeners.giveTransciever(e.getPlayer());
        }

    }

    @EventHandler
    public void handleArenaDeath(PlayerDeathEvent e) {
        // Don't broadcast the death message to everyone
        e.setDeathMessage("");
        // Send to players in arena
        var killer = e.getEntity().getKiller();
        // Check if there is a killer
        if (killer != null && killer != e.getEntity()) {
            // Update the kill count of the killer
            var bits = killer.getUniqueId();
            var currentKills = cache.getIfPresent(bits);
            var newKills = (currentKills != null) ? currentKills.intValue() + 1 : 1;

            cache.put(bits, newKills);
            // random drops
            if (newKills >= 2) {
                e.getDrops().add(new ItemBuilder(random.nextBoolean() ? Material.OAK_PLANKS : Material.BOOK).build());
            }
        }
        // DROPS

        e.getDrops().forEach(all -> {
            if (all.getType() == Material.OAK_PLANKS || all.getType() == Material.BOOK)
                return;
            all.setType(Material.AIR);

        });
        e.getDrops().add(new ItemBuilder(Material.DIAMOND).amount(2).build());
        e.getDrops().add(new ItemBuilder(Material.ARROW).amount(4).build());
        e.getDrops().add(new ItemBuilder(Material.GOLDEN_APPLE).amount(2).build());
        var loc = e.getEntity().getLocation();
        loc.getWorld().spawn(loc, ExperienceOrb.class).setExperience(7);

    }

    private Location getRandomLocation(World world) {
        int x = (random.nextBoolean() ? 1 : -1) * random.nextInt(radius / 2);
        int z = (random.nextBoolean() ? 1 : -1) * random.nextInt(radius / 2);
        return world.getHighestBlockAt(centerCoordinate.getRadiusX() + x, centerCoordinate.getRadiusZ() + z)
                .getLocation().add(0.0, 2.0, 0.0);
    }

    public void giveKit(final Player player) {

        // Clear experience
        player.setLevel(0);
        player.setExp(0);
        player.setHealth(20.0);
        // Obtain player's inventory
        final var inv = player.getInventory();
        // Set Armour
        inv.setHelmet(new ItemBuilder(Material.IRON_HELMET).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build());
        inv.setChestplate(new ItemBuilder(Material.IRON_CHESTPLATE).enchant(Enchantment.PROTECTION_PROJECTILE, 2).build());
        inv.setLeggings(new ItemBuilder(Material.IRON_LEGGINGS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build());
        inv.setBoots(new ItemBuilder(Material.IRON_BOOTS).enchant(Enchantment.PROTECTION_PROJECTILE, 2).build());

        inv.setItem(0, new ItemBuilder(Material.IRON_SWORD).enchant(Enchantment.DAMAGE_ALL).build());
        inv.setItem(1, new ItemBuilder(Material.BOW).enchant(Enchantment.ARROW_DAMAGE).build());
        inv.setItem(2, new ItemBuilder(Material.IRON_AXE).build());
        inv.setItemInOffHand(new ItemBuilder(Material.SHIELD).build());
        inv.setItem(6, new ItemBuilder(Material.OAK_PLANKS).amount(4).build());
        inv.setItem(6, new ItemBuilder(Material.ARROW).amount(4).build());
        inv.setItem(5, new ItemBuilder(Material.GOLDEN_APPLE).amount(2).build());

        inv.setItem(4, new ItemBuilder(Material.NETHER_STAR).name(GlobalListeners.getTRANSCEIVER_NAME())
                .enchant(Enchantment.VANISHING_CURSE).flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES).build());

    }

    public void teleportPlayer(Player player) {
        // Scatter the players
        player.teleport(getRandomLocation(player.getWorld()));
    }

    // vida solo visible por arena players

    // cancelar si no estas en arena
    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        var entity = e.getEntity();

        if (entity instanceof Player) {
            var player = (Player) e.getEntity();
            if (!isInArena(player)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDropEvent(PlayerDropItemEvent e) {
        var player = e.getPlayer();
        if (!isInArena(player))
            e.setCancelled(true);
        else {
            switch (e.getItemDrop().getItemStack().getType()) {
                case GOLDEN_APPLE:
                case DIAMOND:
                case ARROW:
                case STICK:
                case OAK_PLANKS:
                case BOOK:
                case COOKIE:
                case POTION:
                    e.setCancelled(false);
                    break;

                default:
                    e.setCancelled(true);
                    break;
            }
        }

    }

    // crafteos permitidos
    @EventHandler
    public void onCraft(PrepareItemCraftEvent e) {
        var player = (Player) e.getView().getPlayer();
        var isIn = isInArena(player);
        if (isIn) {
            if(e.getRecipe() == null) return;
            var result = e.getRecipe().getResult();
            if (result != null) {
                switch (result.getType()) {
                    case STICK:
                    case DIAMOND_CHESTPLATE:
                    case DIAMOND_LEGGINGS:
                    case DIAMOND_BOOTS:
                    case DIAMOND_SWORD:
                    case DIAMOND_HELMET:
                    case DIAMOND_AXE:
                        return;
                    default:
                        e.getInventory().setResult(new ItemStack(Material.AIR));
                        break;
                }
            }

        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        final var player = e.getPlayer();
        final var inArena = isInArena(player);
        if (inArena) {
            if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                var lastDamage = (EntityDamageByEntityEvent) player.getLastDamageCause();
                var damager = lastDamage.getEntity();
                player.damage(1000, damager);
            } else {
                player.damage(1000);
            }
            cache.invalidate(player.getUniqueId());

        }
    }

    // cancel damage anvil
    @EventHandler
    public void onAnvilDamage(AnvilDamagedEvent e) {
        e.setCancelled(true);
    }
    // infinite lapis

    @EventHandler
    public void openInventoryEvent(InventoryOpenEvent e) {
        if (e.getInventory() instanceof EnchantingInventory) {
            e.getInventory().setItem(1, lapis);
        }
    }

    @EventHandler
    public void closeInventoryEvent(InventoryCloseEvent e) {
        if (e.getInventory() instanceof EnchantingInventory) {
            e.getInventory().setItem(1, null);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        ItemStack item = e.getCurrentItem();
        if (inv instanceof EnchantingInventory) {

            if (item.getType().equals(lapis.getType())) {
                e.setCancelled(true);
            } else {
                e.getInventory().setItem(1, lapis);
            }
        }
    }

}