package us.jcedeno.hangar.paper.commands;

import org.bukkit.entity.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.tranciever.guis.tranceiver.RecieverGUI;

@CommandAlias("debug")
public class DebugCMD extends BaseCommand {
    private Hangar instance;

    public DebugCMD(Hangar instance) {
        this.instance = instance;
    }

    @Default
    public void creator(Player player) {
        new RecieverGUI("Tranceiver", instance, player).open(player);

    }

}
