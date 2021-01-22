package us.jcedeno.hangar.paper.commands;

import java.io.IOException;

import com.google.gson.JsonArray;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Name;
import co.aikar.commands.annotation.Subcommand;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.condor.NewCondor;
import us.jcedeno.hangar.paper.tranciever.guis.tranceiver.RecieverGUI;

@CommandAlias("debug")
public class DebugCMD extends BaseCommand {
    private Hangar instance;

    public DebugCMD(Hangar instance) {
        this.instance = instance;
        var compute = NewCondor.getAvailability("ewr");
        instance.getCommandManager().getCommandCompletions().registerAsyncCompletion("instance-types", c -> {
            return compute;
        });
    }

    @Default
    public void creator(Player player) {
        new RecieverGUI("Tranceiver", instance, player, instance.getCommunicatorManager().getMap().get(
                NewCondor.getTokenMap().getOrDefault(player.getUniqueId().toString(), player.getUniqueId().toString())))
                        .open(player);

    }

    @Subcommand("whitelist")
    public void setWhitelistId(Player player, @Name("condor-whitelist-id") String whitelistID) {
        NewCondor.getCustomWhitelistId().put(player.getUniqueId().toString(),
                whitelistID.equalsIgnoreCase("default") ? "null" : whitelistID);

        player.sendMessage(
                ChatColor.GREEN + (whitelistID.equalsIgnoreCase("default") ? "Your whitelist ID is now default"
                        : "Your whitelist ID  has been updated."));
    }

    @CommandPermission("condor.list")
    @Subcommand("instances")
    public void getInstances(Player player) {
        if (player.hasPermission("condor.list.all")) {
            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                try {
                    var result = NewCondor.get("6QR3W05K3F", "billing?onlyActive=true");
                    var instances = NewCondor.gson.fromJson(result, JsonArray.class);
                    player.sendMessage(instances.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                    player.sendMessage("Condor couldn't complete request.");
                }
            });

        } else {
            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                try {
                    var uuid = player.getUniqueId().toString();
                    var result = NewCondor.get(NewCondor.getTokenMap().getOrDefault(uuid, uuid),
                            "billing?onlyActive=true");
                    var instances = NewCondor.gson.fromJson(result, JsonArray.class);
                    player.sendMessage(instances.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                    player.sendMessage("Condor couldn't complete request.");
                }
            });

        }
    }

    @CommandCompletion("@instance-types")
    @Subcommand("instance-type")
    public void setInstanceType(Player player, @Name("instance-type") String instanceType) {
        NewCondor.getCustomInstanceType().put(player.getUniqueId().toString(), instanceType);

        player.sendMessage(ChatColor.GREEN + "Your have set your default instance type to " + instanceType);
    }

}
