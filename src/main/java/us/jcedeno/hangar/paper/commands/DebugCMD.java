package us.jcedeno.hangar.paper.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.tranciever.utils.GeneralizedInputTask;

@CommandAlias("debug")
@CommandPermission("hangar.debug")
public class DebugCMD extends BaseCommand {
    private Hangar instance;

    public DebugCMD(Hangar instance) {
        this.instance = instance;
    }

    @Subcommand("create")
    public void creator(Player player) {
        GeneralizedInputTask.of(player, instance, (inputTask) -> {
            player.sendMessage("PROVIDE INPUT");

        }, response -> {
            Bukkit.broadcastMessage("Provided input: " + response);
        }).start(5l, 20L);

    }

}
