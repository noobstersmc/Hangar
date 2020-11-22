package us.jcedeno.hangar.paper.commands;

import org.bukkit.entity.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.tranciever.guis.tranceiver.RecieverGUI;

@CommandAlias("debug")
@CommandPermission("hangar.debug")
public class DebugCMD extends BaseCommand {
    private Hangar instance;
    private RecieverGUI mainMenu;

    public DebugCMD(Hangar instance) {
        this.instance = instance;
        this.mainMenu = new RecieverGUI("Tranceiver", instance);
    }

    @Default
    public void creator(Player player) {
        mainMenu.openChildren(player);

    }

}
