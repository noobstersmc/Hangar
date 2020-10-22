package us.jcedeno.hangar.paper.commands;

import org.bukkit.command.CommandSender;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import us.jcedeno.hangar.paper.Hangar;

@CommandAlias("config")
public @RequiredArgsConstructor class SlotCMD extends BaseCommand {
    private @NonNull Hangar instance;

    @Default 
    @CommandPermission("uhc.config.slots")
    @Subcommand("setslots|slots|newslots")
    @CommandAlias("setslots|slots|newslots")
    public void changeSlots(CommandSender sender, Integer newSlots) {
        instance.setMaxSlots(newSlots);
        sender.sendMessage("Slots changed to " + newSlots);
    }
}