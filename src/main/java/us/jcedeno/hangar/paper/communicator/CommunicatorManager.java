package us.jcedeno.hangar.paper.communicator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.messaging.PluginMessageListener;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.condor.NewCondor;
import us.jcedeno.hangar.paper.objects.ProxyChangeInPlayersEvent;
import us.jcedeno.hangar.paper.tranciever.RapidInv;
import us.jcedeno.hangar.paper.tranciever.guis.browser.BrowserWindow;
import us.jcedeno.hangar.paper.tranciever.guis.tranceiver.RecieverGUI;
import us.jcedeno.hangar.paper.tranciever.utils.ServerData;
import us.jcedeno.hangar.paper.uhc.GameData;

public class CommunicatorManager implements PluginMessageListener {
    private Hangar instance;
    private @Getter SelectorInventory serverGui = new SelectorInventory(InventoryType.HOPPER, "UHC Servers");
    private @Getter Cache<String, GameData> cache = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build();
    private @Getter Integer proxyPlayers = 0;
    private @Getter RedisClient redisClient;
    private @Getter StatefulRedisConnection<String, String> redisConnection;
    private @Getter RedisAsyncCommands<String, String> commands;
    private @Getter Set<ServerData> cachedData = new HashSet<>();
    private @Getter HashMap<String, JsonObject> map = new HashMap<String, JsonObject>();

    private @Getter HashMap<String, String> uuidProfile = new HashMap<>();

    HashMap<String, Long> cooldown = new HashMap<>();
    private static Gson gson = new Gson();

    public CommunicatorManager(Hangar instance) {
        this.instance = instance;
        // LETTUCE
        this.redisClient = RedisClient.create(
                "redis://Gxb1D0sbt3VoyvICOQKC8IwakpVdWegW@redis-11764.c73.us-east-1-2.ec2.cloud.redislabs.com:11764/0");
        this.redisConnection = redisClient.connect();
        this.commands = redisConnection.async();

        // Register communication channel to proxy
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(instance, "BungeeCord");
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(instance, "BungeeCord", this);

        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
            try {
                // Refresh the proxyPlayers variable.
                getCount();
                var allData = gson.fromJson(NewCondor.getAllData(), JsonObject.class);
                var serverData = allData.getAsJsonObject("json_info");
                var dataEntrySet = serverData.entrySet();
                // Clear current data.
                cachedData.clear();
                // Recieved data empty
                if (!dataEntrySet.isEmpty()) {
                    var server_data = dataEntrySet.stream().map(all -> gson.fromJson(all.getValue(), ServerData.class))
                            .sorted((h1, h2) -> h1.getGame_id().compareTo(h2.getGame_id())).collect(Collectors.toSet());

                    cachedData.addAll(server_data);
                }

                allData.getAsJsonArray("profiles").forEach(all -> {
                    var element = all.getAsJsonObject();
                    map.put(element.get("token").getAsString(), element);
                });

                var tokens = allData.getAsJsonObject("tokens");
                if (tokens != null) {
                    tokens.entrySet().forEach(to -> {
                        NewCondor.getTokenMap().put(to.getKey(), to.getValue().getAsString());
                    });
                }

                Bukkit.getOnlinePlayers().forEach(all -> {
                    var inv = all.getOpenInventory().getTopInventory();
                    if (inv.getHolder() instanceof RapidInv) {
                        if (inv.getHolder() instanceof BrowserWindow) {
                            var browser = (BrowserWindow) inv.getHolder();
                            browser.update(cachedData);
                        } else if (inv.getHolder() instanceof RecieverGUI) {
                            var reciever = (RecieverGUI) inv.getHolder();
                            var update = getMap().get(NewCondor.getTokenMap().getOrDefault(all.getUniqueId().toString(),
                                    all.getUniqueId().toString()));
                            reciever.update(cachedData, update);
                        }
                    }
                });
            } catch (Exception io) {
                io.printStackTrace();
            }

        }, 0L, 19L);

    }

    @Data
    @AllArgsConstructor(staticName = "of")
    public static class transfer_request {
        String player_name;
        String ip;
        String game_id;
    }

    public void sendToGame(Player player, GameData game_data) {
        var value = cooldown.get(player.getName());
        if (value != null && (System.currentTimeMillis() - value) <= 2000) {
            player.sendMessage("You must wait 2 seconds to connect again.");
            return;
        }
        cooldown.put(player.getName(), System.currentTimeMillis());
        // Send the request to channel condor-transfer for proxy to read it
        System.out.println(
                "Sending " + player.getName() + " to " + game_data.getIp() + " (" + game_data.getGameID() + ")");

        var json_request = new JsonObject();
        json_request.addProperty("type", "connect");
        json_request.addProperty("uuid", player.getUniqueId().toString());
        json_request.addProperty("ip", game_data.getIp());
        json_request.addProperty("condor_id", game_data.getGameID());
        commands.publish("condor", json_request.toString());
        System.out.println(json_request.toString());
    }

    public void sendToIP(Player player, String ip, String ID) {
        var value = cooldown.get(player.getName());
        if (value != null && (System.currentTimeMillis() - value) <= 2000) {
            player.sendMessage("You must wait 2 seconds to connect again.");
            return;
        }
        cooldown.put(player.getName(), System.currentTimeMillis());
        System.out.println("Sending " + player.getName() + " to " + ip + " (" + ID + ")");

        var json_request = new JsonObject();
        json_request.addProperty("type", "connect");
        json_request.addProperty("uuid", player.getUniqueId().toString());
        json_request.addProperty("ip", ip);
        json_request.addProperty("condor_id", ID);
        commands.publish("condor", json_request.toString());
        System.out.println(json_request.toString());
    }

    public void setMetaForUHC(ItemMeta meta, GameData game_data) {

        final var stage = game_data.getGameStage();
        final var titleColor = ChatColor.of("#82abba");
        var name = (game_data.getHostname() != null ? game_data.getHostname() + "'s " : "") + "UHC";
        meta.setDisplayName(ChatColor.of("#f64658") + "" + ChatColor.BOLD + name);

        switch (stage.toLowerCase()) {
            case "ingame": {
                meta.setLore(LoreBuilder.of(
                        titleColor + "Config: " + ChatColor.WHITE + game_data.getGameType() + " "
                                + game_data.getScenarios(),
                        "", titleColor + "Game Time: " + ChatColor.WHITE + timeConvert(game_data.getGameTime()),
                        titleColor + "Players Alive: " + ChatColor.WHITE + game_data.getPlayersAlive(),
                        titleColor + "Spectators: " + ChatColor.WHITE + game_data.getSpectators()));

                break;
            }
            default: {
                meta.setLore(LoreBuilder.of(
                        titleColor + "Config: " + ChatColor.WHITE + game_data.getGameType() + " "
                                + game_data.getScenarios(),
                        "", titleColor + "Players: " + ChatColor.WHITE + game_data.getPlayersOnline() + "/"
                                + game_data.getUhcslots()));

                break;
            }
        }
    }

    public ItemMeta getItemFromEntry(Entry<String, GameData> entry) {
        final var data = entry.getValue();

        final var meta = new ItemStack(Material.STONE).getItemMeta();
        final var stage = data.getGameStage();
        final var titleColor = ChatColor.of("#8c7373");
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
