package us.jcedeno.hangar.paper;

import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.FastInvManager;
import fr.mrmicky.fastinv.ItemBuilder;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.uhc.GameData;
import us.jcedeno.hangar.paper.vultr.VultrAPI;

public class Hangar extends JavaPlugin {
    private @Getter FastInv serverGui = new FastInv(InventoryType.HOPPER, "UHC Servers");
    // private @Getter HashMap<String, GameData> gameDataMap = new HashMap<>();
    private @Getter Cache<String, GameData> cache = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build();

    // GUI tutorial: https://github.com/MrMicky-FR/FastInv
    // Scoreboard Tutorial: https://github.com/MrMicky-FR/FastBoard
    // Commands Tutorial: https://github.com/aikar/commands/wiki/Using-ACF
    @Override
    public void onEnable() {
        FastInvManager.register(this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        Bukkit.getPluginManager().registerEvents(new GlobalListeners(this), this);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
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

        }, 0L, 10L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            // Clean up from before
            serverGui.removeItems(0, 1, 2, 3, 4);
            int i = 0;
            for (var entry : cache.asMap().entrySet()) {
                var item = getItemFromEntry(entry, Material.ENCHANTED_GOLDEN_APPLE);
                serverGui.setItem(i++, item, (e) -> {
                    e.getWhoClicked().sendMessage("Sending you to " + entry.getKey());
                    sendToProxy((Player) e.getWhoClicked(), entry.getKey());
                    e.setCancelled(true);
                });
            }
            // Check if no data is on the GUI
            var first = serverGui.getInventory().getContents()[0];
            // Add a default item if so
            if (first == null || first.getType() == Material.AIR) {
                serverGui.addItem(new ItemBuilder(Material.APPLE)
                        .name(ChatColor.of("#c73838") + "" + ChatColor.BOLD + "No UHCs Running")
                        .enchant(Enchantment.DURABILITY).flags(ItemFlag.HIDE_ENCHANTS)
                        .addLore(
                                ChatColor.WHITE + "Check " + ChatColor.BLUE + "discord.noobsters.net " + ChatColor.WHITE
                                        + "or",
                                ChatColor.AQUA + "@NoobstersUHC " + ChatColor.WHITE + "for upcoming games.")
                        .build());

            }
        }, 0L, 2L);

    }

    public ItemStack getItemFromEntry(Entry<String, GameData> entry, Material material) {
        final var data = entry.getValue();
        final var stage = data.getGameStage();
        final var titleColor = ChatColor.of("#8c7373");
        var itemBuilder = new ItemBuilder(material).name(ChatColor.of("#c73838") + "" + ChatColor.BOLD
                + (data.getHostname() != null ? data.getHostname() + "'s " : "") + "UHC");
        switch (stage.toLowerCase()) {
            case "lobby":
            case "scatter": {
                itemBuilder = itemBuilder.addLore(
                        titleColor + "Players: " + ChatColor.WHITE + data.getPlayersOnline() + "/" + data.getUhcslots(),
                        titleColor + "Config: " + ChatColor.WHITE + data.getGameType() + " " + data.getScenarios());

                break;
            }
            case "ingame": {

                break;
            }
        }

        return itemBuilder.build();
    }

    public void sendToProxy(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ConnectOther");
        out.writeUTF(player.getName());
        out.writeUTF(server);

        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    String plainString(String[] arr) {
        String str = "";
        for (String string : arr) {
            str += string + " ";
        }
        return str;
    }

    @Override
    public void onDisable() {

    }

}
