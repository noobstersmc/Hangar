package us.jcedeno.hangar.paper;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import fr.mrmicky.fastinv.ItemBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * InnerArena
 */
@RequiredArgsConstructor
@CommandAlias("arena|practice|a|p|ffa")
public class Arena extends BaseCommand implements Listener {
    // Long is the most significant bits of player's UUID. Integer is the player's
    private @Getter Cache<Long, Integer> cache = Caffeine.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    private @NonNull Hangar instance;

    // Commands
    @Default
    @CommandPermission("hangar.arena.join")
    public void onPratice(Player player) {
        if(isInArena(player)){
            cache.invalidate(player.getUniqueId().getMostSignificantBits());
            player.sendActionBar("Leaving the arena...");
        }else{
            player.sendActionBar("Joining the arena...");
            cache.put(player.getUniqueId().getMostSignificantBits(), 0);
        }

    }

    private boolean isInArena(Player player){
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
    public void handleArenaDeath(PlayerDeathEvent e) {
        // Don't broadcast the death message to everyone
        e.setDeathMessage("");
        // Send to players in arena
        var killer = e.getEntity().getKiller();
        // Check if there is a killer
        if (killer != null) {
            // Update the kill count of the killer
            var bits = killer.getUniqueId().getMostSignificantBits();
            var currentKills = cache.getIfPresent(killer.getUniqueId().getMostSignificantBits());
            cache.put(bits, (currentKills != null ? currentKills.intValue() : 0) + 1);
        }
        // DROPS

    }

    public void giveKit(final Player player) {
        // Clear experience
        player.setLevel(0);
        player.setExp(0);
        // Obtain player's inventory
        final var inv = player.getInventory();
        // Set Armour
        inv.setHelmet(new ItemBuilder(Material.IRON_HELMET).enchant(Enchantment.VANISHING_CURSE)
                .enchant(Enchantment.PROTECTION_ENVIRONMENTAL).build());
        inv.setChestplate(new ItemBuilder(Material.IRON_CHESTPLATE).enchant(Enchantment.VANISHING_CURSE)
                .enchant(Enchantment.PROTECTION_ENVIRONMENTAL).build());
        inv.setLeggings(new ItemBuilder(Material.IRON_LEGGINGS).enchant(Enchantment.VANISHING_CURSE)
                .enchant(Enchantment.PROTECTION_ENVIRONMENTAL).build());
        inv.setBoots(new ItemBuilder(Material.IRON_BOOTS).enchant(Enchantment.VANISHING_CURSE)
                .enchant(Enchantment.PROTECTION_ENVIRONMENTAL).build());
        inv.setItem(0, new ItemBuilder(Material.IRON_SWORD).enchant(Enchantment.VANISHING_CURSE).build());
        inv.setItem(1, new ItemBuilder(Material.BOW).enchant(Enchantment.VANISHING_CURSE)
                .enchant(Enchantment.ARROW_DAMAGE).build());
        inv.setItem(2, new ItemBuilder(Material.IRON_AXE).enchant(Enchantment.VANISHING_CURSE).build());
        inv.setItemInOffHand(new ItemBuilder(Material.SHIELD).enchant(Enchantment.VANISHING_CURSE).build());
        inv.setItem(8, new ItemBuilder(Material.OAK_PLANKS).build());
        inv.setItem(6, new ItemBuilder(Material.ARROW).amount(4).build());
        inv.setItem(5, new ItemBuilder(Material.GOLDEN_APPLE).amount(2).build());

    }
    //cancelar si no estas en arena
    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent e) {
        var result = e.getInventory().getResult();
        if (result == null || result.getType() == Material.AIR) {
            return;
        }
        if (isGearOrArmor(result.getType())) {
            ItemStack stack = result.clone();
            ItemMeta meta = stack.getItemMeta();
            meta.addEnchant(Enchantment.VANISHING_CURSE, 1, false);
            stack.setItemMeta(meta);
            e.getInventory().setResult(stack);
        }
    }

    boolean isGearOrArmor(Material material) {
        var mat = material.toString();
        return mat != null && mat.contains("DIAMOND") || mat.contains("IRON");
    }

}