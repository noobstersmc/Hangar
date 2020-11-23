package us.jcedeno.hangar.paper;

import java.io.File;
import java.io.FileWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.destroystokyo.paper.Namespaced;
import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
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
import org.bukkit.util.Vector;

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
import us.jcedeno.hangar.paper.arena.ArenaPlayerData;
import us.jcedeno.hangar.paper.arena.ArenaPlayerInventory;
import us.jcedeno.hangar.paper.arena.BlockRestoreTask;
import us.jcedeno.hangar.paper.arena.InventorySerializer;
import us.jcedeno.hangar.paper.arena.KillStreakHandler;
import us.jcedeno.hangar.paper.events.KillStreakEvent;
import us.jcedeno.hangar.paper.objects.CoordinatePair;

/**
 * InnerArena
 */
@CommandAlias("arena|practice|a|p|ffa")
public class Arena extends BaseCommand implements Listener {
    // Objects to control the data for random teleport
    private @Getter @Setter CoordinatePair centerCoordinate = CoordinatePair.of(-160, 0, -300);
    private @Getter @Setter int radius = 140;
    private final List<Namespaced> destroyableKeys = Arrays.asList(NamespacedKey.minecraft("end_stone"),
            NamespacedKey.minecraft("ancient_debris"), NamespacedKey.minecraft("gold_block"));
    // Blocks that should be restored.
    private final List<BlockRestoreTask> restoreTasks = new ArrayList<>();
    // An instance of the plugin
    private @Getter int arenaLimits = 30;
    private Hangar instance;
    // Loading cacche to self expire players
    private @Getter HashMap<UUID, ArenaPlayerData> arenaUsers = new HashMap<>();
    private @Getter HashMap<UUID, ArenaPlayerData> dataToRestore = new HashMap<>();
    // Auto Lapiz
    private final ItemStack lapis = new ItemBuilder(Material.LAPIS_LAZULI).amount(64).build();
    private final Random random = new Random();
    // Local Scoreboard for hearts
    private Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    // File name for arena-data
    private static String ARENA_JSON = Bukkit.getWorldContainer().getPath() + File.separatorChar + "arena-data.json";
    private static String TASKS_JSON = Bukkit.getWorldContainer().getPath() + File.separatorChar + "arena-tasks.json";
    private @Getter KillStreakHandler streakHandler;

    public Arena(Hangar instance) {
        this.instance = instance;
        // Register listener and command.
        this.instance.getCommandManager().registerCommand(this);
        Bukkit.getPluginManager().registerEvents(this, instance);
        // Register health objectives for arena.
        scoreboard.registerNewObjective("health2", Criterias.HEALTH, ChatColor.DARK_RED + "❤", RenderType.INTEGER)
                .setDisplaySlot(DisplaySlot.PLAYER_LIST);
        scoreboard.registerNewObjective("health", Criterias.HEALTH, ChatColor.DARK_RED + "❤", RenderType.HEARTS)
                .setDisplaySlot(DisplaySlot.BELOW_NAME);
        // Handle the kill streaks.
        this.streakHandler = new KillStreakHandler(instance);
    }

    // Commands
    @Default
    public void onPratice(Player player) {
        if (arenaUsers.size() >= this.arenaLimits && !player.hasPermission("reserved.slot")) {
            player.sendMessage(ChatColor.GREEN + "Arena is full! \n Get your rank at " + ChatColor.GOLD
                    + "noobstersuhc.buycraft.net");
            return;
        }
        if (isInArena(player)) {
            leaveArena(player);
        } else {
            player.sendActionBar("Joining the arena...");
            player.setScoreboard(scoreboard);
            player.setGameMode(GameMode.ADVENTURE);
            hideOthers(player);
            giveKit(player);
            teleportPlayer(player);
            var arenaPlayerData = new ArenaPlayerData(player.getUniqueId());
            arenaPlayerData.setLastDamageTime(System.currentTimeMillis());
            arenaPlayerData.setCurrentKills(0);
            arenaUsers.put(player.getUniqueId(), arenaPlayerData);
        }

    }

    @Subcommand("save")
    public void saveCurrentData(CommandSender sender) {
        var gson = new GsonBuilder().setPrettyPrinting().create();
        saveRestoreTaks(gson, sender);

    }

    void savePlayerData(Gson gson, CommandSender sender) {
        arenaUsers.entrySet().forEach(entry -> {
            var player = Bukkit.getPlayer(entry.getKey());
            var data = entry.getValue();

            if (player != null && player.isOnline()) {
                data.serializeInventory(player.getInventory());
                data.setSerializedLocation(player.getLocation());
            }
        });
        try {
            var writer = new FileWriter(ARENA_JSON);
            gson.toJson(arenaUsers.values(), writer);
            writer.flush();
            writer.close();
            sender.sendMessage("Succesfully backed up arena-data!");
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    void saveRestoreTaks(Gson gson, CommandSender sender) {
        try {
            var writer = new FileWriter(TASKS_JSON);
            gson.toJson(restoreTasks, writer);
            writer.flush();
            writer.close();
            sender.sendMessage("Succesfully backed up tasks-data!");
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    @Subcommand("load")
    public void loadArenaData(CommandSender sender) {
        var gson = new GsonBuilder().setPrettyPrinting().create();
        loadPlayerData(gson, sender);
        loadRestoreTaks(gson, sender);

    }

    @Subcommand("rgb")
    public void call(Player sender) {

        var s = instance.getScoreboardManager().getBoard(sender.getUniqueId());
        var arrayOfColor = new ArrayList<java.awt.Color>();
        for (float i = 0; i < 1.0f; i += 0.013f) {
            var color = java.awt.Color.getHSBColor(i, 1.0f, 1.0f);
            arrayOfColor.add(color);
        }

        var letras = "NOOBSTERS".toCharArray();

        for (int i = 0; i < arrayOfColor.size(); i++) {
            var builder = new StringBuilder();
            int j = 0;

            try {
                for (var o : letras) {
                    builder.append(ChatColor.of(arrayOfColor.get(i + j % (arrayOfColor.size()-2)).brighter()) + "" + ChatColor.BOLD + "");
                    builder.append(o);
                    j++;
                }
                Bukkit.getScheduler().runTaskLaterAsynchronously(instance, ()->{
                    s.updateTitle(builder.toString());
                }, 1+ i);
                
            } catch (Exception e) {
                //TODO: handle exception
            }
        }

    }

    @Subcommand("random")
    public void addRandomData(CommandSender sender) {
        for (int i = 0; i < 10; i++) {

            Bukkit.getPluginManager().callEvent(new KillStreakEvent(UUID.randomUUID(), random.nextInt(100) + 1));
        }

    }

    void loadPlayerData(Gson gson, CommandSender sender) {
        try {
            Reader reader = Files.newBufferedReader(Paths.get(ARENA_JSON));
            var jsonArray = gson.fromJson(reader, JsonArray.class);

            jsonArray.forEach(elements -> {
                var data = gson.fromJson(elements, ArenaPlayerData.class);
                var player = Bukkit.getPlayer(data.getUuid());
                if (player != null && player.isOnline()) {
                    restoreArenaPlayer(player, data);
                } else {
                    dataToRestore.put(data.getUuid(), data);
                }
            });
            sender.sendMessage("Succesfully loaded up arena-data!");
            reader.close();
            new File(ARENA_JSON).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void loadRestoreTaks(Gson gson, CommandSender sender) {
        try {
            Reader reader = Files.newBufferedReader(Paths.get(TASKS_JSON));
            var jsonArray = gson.fromJson(reader, JsonArray.class);
            restoreTasks.clear();

            jsonArray.forEach(elements -> {
                var data = gson.fromJson(elements, BlockRestoreTask.class);
                var block = data.getCoordinatePair().toBlock(Bukkit.getWorld(data.getWorld()));
                if (System.currentTimeMillis() >= data.getWhen()) {
                    block.setType(data.getRestoreTo());
                } else {
                    var timeLeft = System.currentTimeMillis() - data.getWhen();
                    var ticksLeft = (timeLeft / 1000) * 20;
                    scheduleRestoreTask(block, timeLeft, ticksLeft);
                }
            });
            sender.sendMessage("Succesfully loaded up block-tasks-data!");
            reader.close();
            new File(TASKS_JSON).delete();
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    private void restoreArenaPlayer(final Player player, final ArenaPlayerData data) {
        if (player != null && player.isOnline()) {
            data.setLastDamageTime(System.currentTimeMillis());
            if (data.getPosition() != null) {
                player.teleport(data.getPosition());
            }
            var invContent = data.getPlayerInventory()[1];
            var armorContent = data.getPlayerInventory()[0];
            try {
                player.getInventory().setContents(InventorySerializer.itemStackArrayFromBase64(invContent));
                player.getInventory().setContents(InventorySerializer.itemStackArrayFromBase64(armorContent));

            } catch (Exception e) {
                e.printStackTrace();
            }

            player.sendActionBar("Joining the arena...");
            player.setScoreboard(scoreboard);
            player.setGameMode(GameMode.ADVENTURE);
            hideOthers(player);

            arenaUsers.put(player.getUniqueId(), data);
            player.sendMessage("Welcome back to the arena!");
        }

    }

    private void leaveArena(final Player player) {
        arenaUsers.remove(player.getUniqueId());
        player.sendActionBar("Leaving the arena...");
        GlobalListeners.giveTransciever(player);
        player.teleport(GlobalListeners.getSpawnLoc());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        showEveryone(player);

    }

    @Subcommand("arenaslots")
    @CommandPermission("uhc.admin")
    public void onChangeSlots(CommandSender sender, @Name("new-slots") Integer arg) {
        this.arenaLimits = arg.intValue();
        sender.sendMessage("Arena slots set to " + this.arenaLimits);
    }

    public boolean isInArena(Player player) {
        return arenaUsers.get(player.getUniqueId()) != null;
    }

    @SuppressWarnings("all")
    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent e) {
        if (!isInArena(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

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
    /*
     * Netherite auto regen 3m -> to bedrock GOLD BLOCK AUTOREGEN instant 10% chance
     * to drop gold ingot
     */

    @EventHandler
    public void onBlockBreakRegen(BlockBreakEvent e) {
        var player = e.getPlayer();
        var block = e.getBlock();
        if (player != null) {
            switch (block.getType()) {
                case GOLD_BLOCK:
                    e.setCancelled(true);
                    if (random.nextBoolean()) {
                        var relative = e.getBlock().getRelative(BlockFace.UP);
                        dropCentered(new ItemStack(Material.GOLD_NUGGET, random.nextInt(3) + 1),
                                (relative.getType() == Material.AIR ? relative.getLocation() : block.getLocation()));
                    }

                    break;
                case ANCIENT_DEBRIS:
                    e.setCancelled(true);
                    block.setType(Material.BEDROCK);

                    scheduleRestoreTask(block, 180_000L, 180 * 20L);

                    var relative = e.getBlock().getRelative(BlockFace.UP);

                    dropCentered(new ItemStack(Material.NETHERITE_SCRAP, 1),
                            (relative.getType() == Material.AIR ? relative.getLocation() : block.getLocation()));

                    break;
                case END_STONE:
                    if (block.getX() == -140 && block.getY() == 75 && block.getZ() == -438) {
                        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_ANVIL_PLACE, SoundCategory.VOICE,
                                1.0f, 1.0f);
                    }
                    e.setCancelled(true);
                    break;
                default:
                    break;
            }
        }

    }

    void scheduleRestoreTask(Block block, Long ms, Long ticks) {
        restoreTasks.add(new BlockRestoreTask(block.getWorld().getName(),
                CoordinatePair.of(block.getX(), block.getY(), block.getZ()), Material.ANCIENT_DEBRIS, Material.BEDROCK,
                System.currentTimeMillis() + ms));

        Bukkit.getScheduler().runTaskLater(instance, () -> {
            block.setType(Material.ANCIENT_DEBRIS);
            var iter = restoreTasks.iterator();
            while (iter.hasNext()) {
                var task = iter.next();
                var coord = task.getCoordinatePair();
                if (coord.equalToBlock(block)) {
                    iter.remove();
                    break;
                }
            }

        }, ticks);
    }

    void dropCentered(ItemStack itemStack, Location location) {
        Location centeredLocation = new Location(location.getWorld(), location.getBlockX() + 0.5,
                location.getBlockY() + 0.5, location.getBlockZ() + 0.5);
        location.getWorld().dropItem(centeredLocation, itemStack).setVelocity(new Vector(0.0, 0.1, 0.0));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getOnlinePlayers().stream().filter(all -> isInArena(all))
                .forEach(toHideFrom -> toHideFrom.hidePlayer(instance, e.getPlayer()));
        // Resotore arena data if there is any
        Bukkit.getScheduler().runTaskLater(instance, () -> {
            if (dataToRestore.containsKey(e.getPlayer().getUniqueId()) && !isInArena(e.getPlayer())) {
                var data = dataToRestore.get(e.getPlayer().getUniqueId());
                dataToRestore.remove(e.getPlayer().getUniqueId());
                restoreArenaPlayer(e.getPlayer(), data);
            }
        }, 5L);
    }

    /*
     * Cancel Fall Damage, Entity Damage, and Projectile Damage to player's that are
     * NOT in the arena.
     */
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntityType() == EntityType.PLAYER && (e.getCause() == EntityDamageEvent.DamageCause.FALL
                || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE)) {
            var player = (Player) e.getEntity();
            if (!isInArena(player)) {
                e.setCancelled(true);
            }

        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamageEvent(EntityDamageByEntityEvent e) {
        var damage = e.getDamager();

        if (damage instanceof Player) {
            var player = (Player) damage;
            if (!isInArena(player)) {
                e.setCancelled(true);
            } else {
                // Anti-AFK
                arenaUsers.get(damage.getUniqueId()).setLastDamageTime(System.currentTimeMillis());
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

    public ArenaPlayerInventory calculatePosition(Player player) {
        var inv = new ArenaPlayerInventory();
        var content = player.getInventory().getContents();
        for (int i = 0; i < content.length; i++) {
            var item = content[i];
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            switch (item.getType()) {
                case IRON_AXE: {
                    inv.setAxe(i);
                    break;
                }
                case DIAMOND_AXE: {
                    if (inv.getAxe() >= -1)
                        continue;
                    inv.setAxe(i);
                    break;
                }
                case DIAMOND_SWORD: {
                    inv.setSword(i);
                    break;
                }
                case IRON_SWORD: {
                    if (inv.getSword() >= -1)
                        continue;
                    inv.setSword(i);
                    break;
                }
                case BOW: {
                    inv.setBow(i);
                    break;
                }
                case ARROW: {
                    inv.setArrows(i);
                    break;
                }
                case GOLDEN_APPLE: {
                    inv.setHealing(i);
                    break;
                }
                case NETHER_STAR: {
                    inv.setTranceiver(i);
                    break;
                }
                default:
                    break;
            }

        }

        return inv;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void respawn(PlayerDeathEvent e) {
        e.getEntity().setHealth(20.0D);
        e.getEntity().setFireTicks(0);
        e.getEntity().getActivePotionEffects().forEach(eff -> e.getEntity().removePotionEffect(eff.getType()));

    }

    @EventHandler
    public void handleArenaDeath(PlayerDeathEvent e) {
        // Don't broadcast the death message to everyone
        e.setDeathMessage("");
        var player = e.getEntity();
        player.setCanPickupItems(false);
        var arenaData = arenaUsers.get(player.getUniqueId());
        if (arenaData != null) {
            if ((System.currentTimeMillis() - arenaData.getLastDamageTime()) >= 60_000) {
                player.sendMessage(
                        ChatColor.RED + "You have been kicked out of the arena for being idle for too long.");
                Bukkit.getScheduler().runTask(instance, () -> leaveArena(player));
            } else {
                arenaData.setInventory(calculatePosition(player));
                if (isInArena(player)) {
                    Bukkit.getScheduler().runTask(instance, () -> {
                        player.setCanPickupItems(true);
                        giveKit(player);
                        player.teleport(getRandomLocation(player.getWorld()));
                    });
                } else {
                    GlobalListeners.giveTransciever(player);
                }
            }
        }
        // Send to players in arena
        var killer = e.getEntity().getKiller();
        // Check if there is a killer
        if (killer != null && killer != e.getEntity()) {
            // Update the kill count of the killer
            var bits = killer.getUniqueId();
            killer.sendActionBar(ChatColor.of("#8652ad") + "You killed " + player.getName());
            var killerData = arenaUsers.get(bits);
            killerData.setCurrentKills(killerData.getCurrentKills() + 1);
            // random drops
            if (killerData.getCurrentKills() >= 2) {
                Bukkit.getPluginManager()
                        .callEvent(new KillStreakEvent(killer.getUniqueId(), killerData.getCurrentKills()));
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
        var data = arenaUsers.get(player.getUniqueId());
        // Clear experience
        player.setLevel(0);
        player.setExp(0);
        player.setHealth(20.0);
        // Obtain player's inventory
        final var inv = player.getInventory();
        inv.clear();
        // Set Armour
        inv.setHelmet(new ItemBuilder(Material.IRON_HELMET).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build());
        inv.setChestplate(
                new ItemBuilder(Material.IRON_CHESTPLATE).enchant(Enchantment.PROTECTION_PROJECTILE, 2).build());
        inv.setLeggings(
                new ItemBuilder(Material.IRON_LEGGINGS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build());
        inv.setBoots(new ItemBuilder(Material.IRON_BOOTS).enchant(Enchantment.PROTECTION_PROJECTILE, 2).build());

        if (data != null) {
            var invPos = data.getInventory();
            inv.setItem((invPos.getSword() > -1 ? invPos.getSword() : 0),
                    new ItemBuilder(Material.IRON_SWORD).enchant(Enchantment.DAMAGE_ALL).build());
            inv.setItem((invPos.getBow() > -1 ? invPos.getBow() : 1),
                    new ItemBuilder(Material.BOW).enchant(Enchantment.ARROW_DAMAGE).build());
            inv.setItem((invPos.getAxe() > -1 ? invPos.getAxe() : 2), new ItemBuilder(Material.IRON_AXE).build());
            inv.setItem((invPos.getTranceiver() > -1 ? invPos.getTranceiver() : 4),
                    new ItemBuilder(Material.NETHER_STAR).name(GlobalListeners.getTRANSCEIVER_NAME())
                            .enchant(Enchantment.VANISHING_CURSE)
                            .flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES).build());
            inv.setItem((invPos.getHealing() > -1 ? invPos.getHealing() : 4),
                    new ItemBuilder(Material.GOLDEN_APPLE).amount(2).build());
            inv.setItem((invPos.getArrows() > -1 ? invPos.getArrows() : 9),
                    new ItemBuilder(Material.ARROW).amount(4).build());
        } else {
            inv.setItem(0, new ItemBuilder(Material.IRON_SWORD).enchant(Enchantment.DAMAGE_ALL).build());
            inv.setItem(1, new ItemBuilder(Material.BOW).enchant(Enchantment.ARROW_DAMAGE).build());
            inv.setItem(2, new ItemBuilder(Material.IRON_AXE).build());
            inv.setItem(4,
                    new ItemBuilder(Material.NETHER_STAR).name(GlobalListeners.getTRANSCEIVER_NAME())
                            .enchant(Enchantment.VANISHING_CURSE)
                            .flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES).build());
            inv.setItem(5, new ItemBuilder(Material.GOLDEN_APPLE).amount(2).build());
            inv.setItem(9, new ItemBuilder(Material.ARROW).amount(4).build());
        }

        if (inv.getItemInOffHand().getType() != Material.AIR) {
            inv.addItem(new ItemBuilder(Material.SHIELD).build());
        } else {
            inv.setItemInOffHand(new ItemBuilder(Material.SHIELD).build());

        }

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

    @EventHandler(priority = EventPriority.HIGH)
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
                case APPLE:
                case NETHERITE_INGOT:
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
            if (e.getRecipe() == null)
                return;
            var result = e.getRecipe().getResult();
            if (result != null) {
                switch (result.getType()) {
                    case DIAMOND_PICKAXE:
                        var meta = result.getItemMeta();
                        meta.setDestroyableKeys(destroyableKeys);
                        result.setItemMeta(meta);
                        e.getInventory().setResult(result);
                    case STICK:
                    case DIAMOND_CHESTPLATE:
                    case DIAMOND_LEGGINGS:
                    case DIAMOND_BOOTS:
                    case DIAMOND_SWORD:
                    case DIAMOND_HELMET:
                    case DIAMOND_AXE:
                    case GOLD_INGOT:
                    case NETHERITE_INGOT:
                    case GOLDEN_APPLE:
                        return;
                    default:
                        e.getInventory().setResult(null);
                        break;
                }
            }

        }

    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent e) {
        if (e.getEntity().getItemStack().getType().equals(Material.ANCIENT_DEBRIS)) {
            e.getEntity().getItemStack().setType(Material.NETHERITE_SCRAP);
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
            arenaUsers.remove(player.getUniqueId());

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