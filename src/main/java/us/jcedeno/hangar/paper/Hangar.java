package us.jcedeno.hangar.paper;

import java.util.HashMap;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.FastInvManager;
import fr.mrmicky.fastinv.ItemBuilder;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.uhc.GameData;
import us.jcedeno.hangar.paper.vultr.VultrAPI;

public class Hangar extends JavaPlugin{
    private @Getter FastInv serverGui = new FastInv(9 * 4, "UHC Servers");
    private @Getter HashMap<String, GameData> gameDataMap = new HashMap<>();

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
                            gameDataMap.put(instances.getId(), data);
                        }
                    });
                }
            });

        }, 0L, 10L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            int i = 0;
            for (var entry : gameDataMap.entrySet()) {
                var gameData = entry.getValue();
                serverGui.setItem(i++,
                        new ItemBuilder(Material.NETHER_STAR)
                                .name(ChatColor.GOLD + "" + ChatColor.BOLD + "UHC " + gameData.getGameID())
                                .lore(ChatColor.WHITE + "Scenarios: " + plainString(gameData.getScenarios()),
                                        ChatColor.WHITE + "Game Time: " + gameData.getGameTime(),
                                        ChatColor.WHITE + "Game Stage: " + gameData.getGameStage())
                                .build(),
                        (e) -> {
                            e.getWhoClicked().sendMessage("Sending you to " + entry.getKey());
                            sendToProxy((Player)e.getWhoClicked(), entry.getKey());
                            e.setCancelled(true);
                        });
            }
        }, 0L, 2L);

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
