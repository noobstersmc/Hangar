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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.messaging.PluginMessageListener;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import redis.clients.jedis.Jedis;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.objects.ProxyChangeInPlayersEvent;
import us.jcedeno.hangar.paper.tranciever.RapidInv;
import us.jcedeno.hangar.paper.tranciever.guis.browser.BrowserWindow;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.TransferRequest;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.UHCData;
import us.jcedeno.hangar.paper.tranciever.guis.tranceiver.RecieverGUI;
import us.jcedeno.hangar.paper.tranciever.utils.ServerData;
import us.jcedeno.hangar.paper.uhc.GameData;

public class CommunicatorManager implements PluginMessageListener {
    private Hangar instance;
    private @Getter SelectorInventory serverGui = new SelectorInventory(InventoryType.HOPPER, "UHC Servers");
    private @Getter Cache<String, GameData> cache = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build();
    private @Getter Integer proxyPlayers = 0;
    private @Getter Jedis jedis;
    private @Getter Set<ServerData> cachedData = new HashSet<>();

    public CommunicatorManager(Hangar instance) {
        this.instance = instance;
        this.jedis = new Jedis("redis-11764.c73.us-east-1-2.ec2.cloud.redislabs.com", 11764);
        this.jedis.auth("Gxb1D0sbt3VoyvICOQKC8IwakpVdWegW");
        // Register communication channel to proxy
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(instance, "BungeeCord");
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(instance, "BungeeCord", this);

        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
            // Refresh the proxyPlayers variable.
            getCount();
            // Obtain data from jedis
            var servers_data = jedis.keys("servers:*");

            if (servers_data.isEmpty()) {
                Bukkit.broadcast("data is empty", "debug");

                cachedData.clear();
                Bukkit.getOnlinePlayers().forEach(all -> {
                    var inv = all.getOpenInventory().getTopInventory();

                    if (inv.getHolder() instanceof RapidInv) {
                        if (inv.getHolder() instanceof BrowserWindow) {
                            var browser = (BrowserWindow) inv.getHolder();
                            browser.update(cachedData);
                        } else if (inv.getHolder() instanceof RecieverGUI) {
                            var reciever = (RecieverGUI) inv.getHolder();
                            reciever.update(cachedData);
                        }
                    }
                });
                return;
            }
            var filtered_data = servers_data.stream().collect(Collectors.toList());
            if (!filtered_data.isEmpty()) {
                
                var list_data = jedis.mget(filtered_data.toArray(new String[] {}));
                var set = new HashSet<ServerData>();

                list_data.forEach(all -> {
                    try {
                        set.add(gson.fromJson(all, ServerData.class));
                    } catch (Exception e) {
                        // e.printStackTrace();
                    }
                });
                cachedData.clear();
                cachedData.addAll(set);
                

                Bukkit.getOnlinePlayers().forEach(all -> {
                    var inv = all.getOpenInventory().getTopInventory();
                    if (inv.getHolder() instanceof RapidInv) {
                        if (inv.getHolder() instanceof BrowserWindow) {
                            var browser = (BrowserWindow) inv.getHolder();
                            browser.update(set);
                        } else if (inv.getHolder() instanceof RecieverGUI) {
                            var reciever = (RecieverGUI) inv.getHolder();
                            reciever.update(set);
                        }
                    }
                });

            }else{
                
            Bukkit.broadcast("empty", "debug");
            }

        }, 25L, 19L);

    }

    @Data
    @AllArgsConstructor(staticName = "of")
    public static class transfer_request {
        String player_name;
        String ip;
        String game_id;
    }

    HashMap<String, Long> cooldown = new HashMap<>();

    public void sendToGame(Player player, GameData game_data) {
        var value = cooldown.get(player.getName());
        if (value != null && (System.currentTimeMillis() - value) <= 2000) {
            player.sendMessage("You must wait 2 seconds to connect again.");
            return;
        }
        cooldown.put(player.getName(), System.currentTimeMillis());
        var name = "game-" + game_data.getGameID().substring(0, 6);
        var request = transfer_request.of(player.getName(), game_data.getIp(), name);
        var request_json = gson.toJson(request);
        // Send the request to channel condor-transfer for proxy to read it
        System.out.println(
                "Sending " + player.getName() + " to " + game_data.getIp() + " (" + game_data.getGameID() + ")");
        Bukkit.getScheduler().runTaskAsynchronously(instance, task -> {
            try {
                jedis.publish("condor-transfer", request_json);

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
    }

    public void sendToIP(Player player, String ip, String ID) {
        var value = cooldown.get(player.getName());
        if (value != null && (System.currentTimeMillis() - value) <= 2000) {
            player.sendMessage("You must wait 2 seconds to connect again.");
            return;
        }
        cooldown.put(player.getName(), System.currentTimeMillis());
        var name = "game-" + ID.substring(0, 6);
        var request = TransferRequest.of(player.getName(), ip, name);
        var request_json = gson.toJson(request);
        // Send the request to channel condor-transfer for proxy to read it
        System.out.println("Sending " + player.getName() + " to " + ip + " (" + name + ")");
        Bukkit.getScheduler().runTaskAsynchronously(instance, task -> {
            try {
                jedis.publish("condor-transfer", request_json);

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
    }

    private static Gson gson = new Gson();

    private UHCData getFromData(String data) {
        var old_format = fromData(data);
        return UHCData.fromOldFormat(old_format);
    }

    private GameData fromData(String data) {
        return gson.fromJson(data, GameData.class);
    }

    private ItemStack getItemFromData(GameData game_data) {
        String type = game_data.getGameType();
        var item = new ItemStack(Material.STONE);
        switch (type.toLowerCase()) {
            case "uhc": {

                item.setType(Material.ENCHANTED_GOLDEN_APPLE);
                var meta = item.getItemMeta();
                setMetaForUHC(meta, game_data);
                item.setItemMeta(meta);
                break;
            }
            case "uhc-run": {
                item.setType(Material.GOLDEN_APPLE);
                var meta = item.getItemMeta();

                setMetaForUHC(meta, game_data);
                item.setItemMeta(meta);
                break;
            }
            default: {
                // TODO: 404 Not found easter egg
                /*
                 * item.setType(Material.SPONGE); var meta = item.getItemMeta();
                 * meta.setDisplayName("Unknown Server"); item.setItemMeta(meta);
                 */
                item.setType(Material.ENCHANTED_GOLDEN_APPLE);
                var meta = item.getItemMeta();

                setMetaForUHC(meta, game_data);
                item.setItemMeta(meta);
                break;
            }
        }

        return item;
    }

    public void setMetaForUHC(ItemMeta meta, GameData game_data) {

        final var stage = game_data.getGameStage();
        final var titleColor = ChatColor.of("#82abba");
        var name = (game_data.getHostname() != null ? game_data.getHostname() + "'s " : "") + "UHC";
        meta.setDisplayName(ChatColor.of("#f64658") + "" + ChatColor.BOLD + name);

        switch (stage.toLowerCase()) {
            case "lobby":
            case "scatter": {
                meta.setLore(LoreBuilder.of(
                        titleColor + "Config: " + ChatColor.WHITE + game_data.getGameType() + " "
                                + game_data.getScenarios(),
                        "", titleColor + "Players: " + ChatColor.WHITE + game_data.getPlayersOnline() + "/"
                                + game_data.getUhcslots()));

                break;
            }
            case "ingame": {
                meta.setLore(LoreBuilder.of(
                        titleColor + "Config: " + ChatColor.WHITE + game_data.getGameType() + " "
                                + game_data.getScenarios(),
                        "", titleColor + "Game Time: " + ChatColor.WHITE + timeConvert(game_data.getGameTime()),
                        titleColor + "Players Alive: " + ChatColor.WHITE + game_data.getPlayersAlive(),
                        titleColor + "Spectators: " + ChatColor.WHITE + game_data.getSpectators()));

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
