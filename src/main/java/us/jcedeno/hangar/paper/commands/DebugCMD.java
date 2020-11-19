package us.jcedeno.hangar.paper.commands;

import org.bukkit.entity.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.tranciever.creator.CreatorGUI;
import us.jcedeno.hangar.paper.tranciever.creator.GameCreator;
import us.jcedeno.hangar.paper.tranciever.guis.RecieverGUI;
import us.jcedeno.hangar.paper.tranciever.guis.UHCGui;

@CommandAlias("debug")
@CommandPermission("hangar.debug")
public class DebugCMD extends BaseCommand {
    private Hangar instance;
    private RecieverGUI recieverGUI;
    private UHCGui uhcGui;

    public DebugCMD(Hangar instance) {
        this.instance = instance;
        this.recieverGUI = new RecieverGUI("Tranceiver");
        this.uhcGui = new UHCGui("UHC Selector");
    }

    @Default
    public void openGui(Player sender) {
        recieverGUI.openChildren(sender);
    }

    @Subcommand("uhc")
    public void other(Player sender) {
        uhcGui.open(sender);
    }

    @Subcommand("create")
    public void creator(Player player) {
        new CreatorGUI("Creator", instance).open(player);
        var game = GameCreator.builder().seed("12134523").build();
        System.out.println(game);

    }

}
