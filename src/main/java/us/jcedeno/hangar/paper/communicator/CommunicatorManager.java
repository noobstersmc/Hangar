package us.jcedeno.hangar.paper.communicator;

import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.messaging.PluginMessageListener;

import fr.mrmicky.fastinv.ItemBuilder;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.objects.ProxyChangeInPlayersEvent;
import us.jcedeno.hangar.paper.uhc.GameData;
import us.jcedeno.hangar.paper.vultr.VultrAPI;

public class CommunicatorManager implements PluginMessageListener {
    private Hangar instance;
    private @Getter SelectorInventory serverGui = new SelectorInventory(InventoryType.HOPPER, "UHC Servers");
    private @Getter Cache<String, GameData> cache = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build();
    private @Getter Integer proxyPlayers = 0;

    public CommunicatorManager(Hangar instance) {
        this.instance = instance;
        // Register communication channel to proxy
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(instance, "BungeeCord");
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(instance, "BungeeCord", this);

        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
            // Refresh the proxyPlayers variable.
            getCount();
            // Clean up the cache
            cache.cleanUp();

            var entries = cache.asMap().entrySet();
            // Add default item if no data online
            if (entries.size() <= 0) {
                serverGui.setItem(0, new ItemBuilder(Material.APPLE)
                        .name(ChatColor.of("#c73838") + "" + ChatColor.BOLD + "No UHCs Running")
                        .enchant(Enchantment.DURABILITY).flags(ItemFlag.HIDE_ENCHANTS)
                        .addLore(
                                ChatColor.WHITE + "Check " + ChatColor.BLUE + "discord.noobsters.net " + ChatColor.WHITE
                                        + "or",
                                ChatColor.AQUA + "@NoobstersUHC " + ChatColor.WHITE + "for upcoming games.")
                        .build());
                serverGui.setItems(1, 4, null);
                return;
            }
            // Keep going and update based on entries
            var entryIter = entries.iterator();
            // Get all items to upate if there already is one in place.
            var currentItems = serverGui.getInventory().getContents();
            // Lopp through the items in the inventory.
            for (int i = 0; i < currentItems.length; i++) {
                var item = currentItems[i];
                var hasNext = entryIter.hasNext();
                // Check if item is air and add or update.
                if (item != null && item.getType() != Material.AIR) {
                    // Is there are no more entries remove the item
                    if (!hasNext) {
                        serverGui.setItem(i, null);
                        continue;
                    }
                    // Get the following entry
                    var next = entryIter.next();
                    // Get meta info
                    var meta = getItemFromEntry(next);
                    item.setItemMeta(meta);
                    //TODO: A switch statmement to represent the server status with the material
                    item.setType(Material.ENCHANTED_GOLDEN_APPLE);
                    // Update server it connects to.
                    serverGui.setItem(i, item, e -> {
                        sendToProxy((Player) e.getWhoClicked(), next.getKey());
                    });
                } else {
                    if (!hasNext) {
                        continue;
                    }
                    // Add a new item
                    var next = entryIter.next();
                    serverGui.addItem(new ItemBuilder(Material.DIAMOND_SWORD).name(next.getKey()).build());
                }
            }

        }, 25L, 5L);

        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
            VultrAPI.getInstances().thenAccept(result -> {
                for (var instances : result) {
                    var ip = instances.getMain_ip();
                    VultrAPI.getGameData("http://" + ip + ":8081").thenAccept(data -> {
                        if (data != null) {
                            cache.put(instances.getId(), data);
                        }
                    });
                }
            });

        }, 0L, 5L);
    }

    public ItemMeta getItemFromEntry(Entry<String, GameData> entry) {
        final var data = entry.getValue();
        final var stage = data.getGameStage();
        final var titleColor = ChatColor.of("#8c7373");

        final var meta = new ItemStack(Material.STONE).getItemMeta();
        var name = (data.getHostname() != null ? data.getHostname() + "'s " : "") + "UHC";
        meta.setDisplayName(ChatColor.of("#c73838") + "" + ChatColor.BOLD + name);

        switch (stage.toLowerCase()) {
            case "lobby":
            case "scatter": {
                meta.setLore(LoreBuilder.of(
                        titleColor + "Config: " + ChatColor.WHITE + data.getGameType() + " " + data.getScenarios(), "",
                        titleColor + "Players: " + ChatColor.WHITE + data.getPlayersOnline() + "/"
                                + data.getUhcslots()));

                break;
            }
            case "ingame": {
                meta.setLore(LoreBuilder.of(
                        titleColor + "Config: " + ChatColor.WHITE + data.getGameType() + " " + data.getScenarios(), "",
                        titleColor + "Game Time: " + ChatColor.WHITE + timeConvert(data.getGameTime()),
                        titleColor + "Players Alive: " + ChatColor.WHITE + data.getPlayersAlive(),
                        titleColor + "Spectators: " + ChatColor.WHITE + data.getSpectators()));

                break;
            }
        }

        return meta;
    }

    private String timeConvert(int t) {
        int hours = t / 3600;

        int minutes = (t % 3600) / 60;
        int seconds = t % 60;

        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds)
                : String.format("%02d:%02d", minutes, seconds);
    }

    public void sendToProxy(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ConnectOther");
        out.writeUTF(player.getName());
        out.writeUTF(server);

        player.sendPluginMessage(instance, "BungeeCord", out.toByteArray());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        // Read server var.
        in.readUTF();

        if (subchannel.equals("PlayerCount")) {
            var playersInProxy = in.readInt();
            if (playersInProxy != this.proxyPlayers) {
                this.proxyPlayers = playersInProxy;
                Bukkit.getPluginManager().callEvent(new ProxyChangeInPlayersEvent(playersInProxy));
            }
        }
    }

    public void getCount() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerCount");
        out.writeUTF("ALL");

        Bukkit.getServer().sendPluginMessage(instance, "BungeeCord", out.toByteArray());

    }

}
