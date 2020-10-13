package us.jcedeno.hangar.paper;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import fr.mrmicky.fastinv.ItemBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import us.jcedeno.hangar.paper.objects.CoordinatePair;

/**
 * InnerArena
 */
@RequiredArgsConstructor
@CommandAlias("arena|practice|a|p|ffa")
public class Arena extends BaseCommand implements Listener {
    // Long is the most significant bits of player's UUID. Integer is the player's
    private @Getter Cache<Long, Integer> cache = Caffeine.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    // Objects to control the data for random teleport
    private @Getter @Setter CoordinatePair centerCoordinate = CoordinatePair.of(-160, 0, -300);
    private @Getter @Setter int radius = 100;
    // An instance of the plugin
    private @NonNull Hangar instance;
    // Auto Lapiz
    private final ItemStack lapis = new ItemBuilder(Material.LAPIS_LAZULI).amount(64).build();
    private final Random random = new Random();

    // Commands
    @Default
    public void onPratice(Player player) {
        if (isInArena(player)) {
            cache.invalidate(player.getUniqueId().getMostSignificantBits());
            player.sendActionBar("Leaving the arena...");
            GlobalListeners.giveTransciever(player);
            player.teleport(GlobalListeners.getSpawnLoc());
        } else {
            player.sendActionBar("Joining the arena...");
            giveKit(player);
            teleportPlayer(player);
            cache.put(player.getUniqueId().getMostSignificantBits(), 0);
        }

    }

    private boolean isInArena(Player player) {
        return cache.getIfPresent(player.getUniqueId().getMostSignificantBits()) != null;
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
    public void onRespawn(PlayerRespawnEvent e) {
        if (isInArena(e.getPlayer())) {
            giveKit(e.getPlayer());
            e.setRespawnLocation(getRandomLocation(e.getPlayer().getWorld()));
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
            var bits = killer.getUniqueId().getMostSignificantBits();
            var currentKills = cache.getIfPresent(killer.getUniqueId().getMostSignificantBits());
            cache.put(bits, (currentKills != null ? currentKills.intValue() : 0) + 1);

            currentKills = cache.getIfPresent(bits);

            // random drops
            if (currentKills % 2 == 0) {
                if (random.nextInt(2) == 1) {
                    e.getDrops().add(new ItemBuilder(Material.OAK_PLANKS).build());
                } else{
                    e.getDrops().add(new ItemBuilder(Material.BOOK).build());
                }
            }
        }
        // DROPS

        e.getDrops().forEach(all -> {
            if (all.getType() == Material.GOLDEN_APPLE || all.getType() == Material.IRON_INGOT || all.getType() == Material.BOOK ) 
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
        inv.setHelmet(new ItemBuilder(Material.IRON_HELMET).enchant(Enchantment.PROTECTION_ENVIRONMENTAL).build());
        inv.setChestplate(
                new ItemBuilder(Material.IRON_CHESTPLATE).enchant(Enchantment.PROTECTION_ENVIRONMENTAL).build());
        inv.setLeggings(new ItemBuilder(Material.IRON_LEGGINGS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL).build());
        inv.setBoots(new ItemBuilder(Material.IRON_BOOTS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL).build());
        
        inv.setItem(0, new ItemBuilder(Material.IRON_SWORD).enchant(Enchantment.DAMAGE_ALL).build());
        inv.setItem(1, new ItemBuilder(Material.BOW).enchant(Enchantment.ARROW_DAMAGE).build());
        inv.setItem(2, new ItemBuilder(Material.IRON_AXE).build());
        inv.setItemInOffHand(new ItemBuilder(Material.SHIELD).build());
        inv.setItem(6, new ItemBuilder(Material.OAK_PLANKS).amount(4).build());
        inv.setItem(6, new ItemBuilder(Material.ARROW).amount(4).build());
        inv.setItem(5, new ItemBuilder(Material.GOLDEN_APPLE).amount(2).build());

    }

    public void teleportPlayer(Player player) {
        // Scatter the players
        player.teleport(getRandomLocation(player.getWorld()));
    }

    // vida solo visible por arena players

    // item drop canceled

    @EventHandler
    public void onItemSpawn(EntityDropItemEvent e) {
        var stack = e.getItemDrop().getItemStack();
        var type = stack.getType();
        if ((type.toString().contains("IRON") && !type.equals(Material.IRON_INGOT)) || type.equals(Material.BOW)) {
            e.setCancelled(true);
        }
    }

    // cancelar si no estas en arena
    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player && !isInArena((Player) e.getEntity()))
            e.setCancelled(true);
    }

    // crafteos permitidos
    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if ((!e.getCurrentItem().getType().toString().contains("DIAMOND"))
                || !e.getCurrentItem().getType().equals(Material.SHIELD)
                || !e.getCurrentItem().getType().equals(Material.STICK))
            e.setCancelled(true);

    }

    // cancel damage anvil

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