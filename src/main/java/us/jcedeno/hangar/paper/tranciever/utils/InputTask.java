package us.jcedeno.hangar.paper.tranciever.utils;

import com.destroystokyo.paper.Title;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.communicator.LoreBuilder;
import us.jcedeno.hangar.paper.tranciever.guis.creator.CreatorGUI;

@RequiredArgsConstructor
public class InputTask extends BukkitRunnable implements Listener {
    private @Getter Player player;
    private @Getter CreatorGUI gui;
    private Hangar instance;
    private String input;

    public InputTask(Player player, CreatorGUI gui, Hangar instance) {
        this.player = player;
        this.gui = gui;
        this.instance = instance;
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    @Override
    public void run() {
        if (player == null || !player.isOnline()) {
            System.out.println("Cancelled creation.");
            this.cancel();
            HandlerList.unregisterAll(this);
            return;
        }
        if (input != null) {
            Bukkit.getScheduler().runTask(instance, () -> gui.open(player));
            this.cancel();
            HandlerList.unregisterAll(this);
            return;
        }
        player.sendTitle(Title.builder().fadeIn(0).fadeOut(30).title(ChatColor.GREEN + "Please")
                .subtitle("type your seed in chat.").build());
    }

    @EventHandler
    public void onChatIntercept(AsyncPlayerChatEvent e) {
        if (e.getPlayer() != player) {
            return;
        }
        input = e.getMessage();
        player.sendMessage("Seed has been set to " + input);
        e.setCancelled(true);
        this.cancel();
        player.resetTitle();
        gui.getInventory().getItem(gui.slot_for_seed).setLore(LoreBuilder.of(ChatColor.WHITE + input));
        gui.getCreatorObject().setSeed(input);
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().runTask(instance, () -> gui.open(player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (e.getPlayer() != player) {
            return;
        }
        this.cancel();
        HandlerList.unregisterAll(this);
    }

}
