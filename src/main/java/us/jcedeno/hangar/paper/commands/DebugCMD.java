package us.jcedeno.hangar.paper.commands;

import java.util.HashSet;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.tranciever.RapidInv;
import us.jcedeno.hangar.paper.tranciever.guis.browser.BrowserWindow;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.GameType;
import us.jcedeno.hangar.paper.tranciever.guis.tranceiver.RecieverGUI;
import us.jcedeno.hangar.paper.tranciever.utils.ServerData;

@CommandAlias("debug")
@CommandPermission("hangar.debug")
public class DebugCMD extends BaseCommand {
    private Hangar instance;

    public DebugCMD(Hangar instance) {
        this.instance = instance;
    }

    @Default
    public void creator(Player player) {
        new RecieverGUI("Tranceiver", instance, player).open(player);

    }

    @Subcommand("update")
    public void update() {
        
        var hashSet = new HashSet<ServerData>();
        var random = new Random();
        hashSet.addAll(ServerData.getDummyData(random.nextInt(20)+1, GameType.UHC));
        hashSet.addAll(ServerData.getDummyData(random.nextInt(20)+1, GameType.RUN));
        hashSet.addAll(ServerData.getDummyData(random.nextInt(20)+1, GameType.MEETUP));
        
        Bukkit.getOnlinePlayers().forEach(all -> {
            var inv = all.getOpenInventory().getTopInventory();
            if (inv.getHolder() instanceof RapidInv) {
                Bukkit.broadcastMessage("Rapid window");
                if (inv.getHolder() instanceof BrowserWindow) {

                    Bukkit.broadcastMessage("Browser window");
                    var browser = (BrowserWindow) inv.getHolder();
                    browser.update(hashSet);
                }
            }
        });
    }

}
