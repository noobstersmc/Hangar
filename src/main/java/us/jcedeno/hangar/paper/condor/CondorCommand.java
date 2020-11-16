package us.jcedeno.hangar.paper.condor;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Name;
import co.aikar.commands.annotation.Subcommand;
import us.jcedeno.hangar.paper.Hangar;

@CommandAlias("condor")
@CommandPermission("condor.create")
public class CondorCommand extends BaseCommand {
    private Hangar instance;

    public CondorCommand(Hangar instance) {
        this.instance = instance;
        instance.getCommandManager().getCommandCompletions().registerStaticCompletion("uhcTypes",
                List.of("UHC", "UHC-Run"));
        instance.getCommandManager().getCommandCompletions().registerStaticCompletion("seed",
                List.of("random", "seed"));
        instance.getCommandManager().registerCommand(this);
    }

    @Subcommand("create")
    @CommandCompletion("@uhcTypes @seed")
    public void createServerCommand(Player sender, @Name("gameType") @Default("UHC") String gameType,
            @Name("seed") @Default("random") String seed) {
        sender.sendMessage("Creating a server for you.");
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try {
                var response = instance.getCondorManager().createMatch(sender.getName(), gameType, "vultr", "us", seed);
                System.out.println(response);
                sender.sendMessage("Server created.");
                
            } catch (Exception e) {
                sender.sendMessage("Server couldn't be created. Please report this error.");
                e.printStackTrace();
            }

        });
    }

}
