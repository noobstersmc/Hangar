package us.jcedeno.hangar.paper.commands;

import org.bukkit.entity.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.tranciever.guis.creator.CreatorGUI;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.GameType;

@CommandAlias("debug")
@CommandPermission("hangar.debug")
public class DebugCMD extends BaseCommand {
    private Hangar instance;

    public DebugCMD(Hangar instance) {
        this.instance = instance;
    }

    @Subcommand("create")
    public void creator(Player player) {
        var creatorGui = new CreatorGUI("UHC Creator", null, instance, GameType.RUN);
        creatorGui.open(player);

    }

}
