package us.jcedeno.hangar.paper.tranciever.utils;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class GeneralizedInputTask extends BukkitRunnable implements Listener {
    private Player player;
    private Plugin plugin;
    private Consumer<GeneralizedInputTask> runConsumer;
    private Consumer<String> input;

    @Override
    public void run() {
        runConsumer.accept(this);
    }

    @EventHandler
    public void onChatIntercept(AsyncPlayerChatEvent e) {
        if (e.getPlayer() != player) {
            return;
        }
        // Return the input
        input.accept(e.getMessage());
        // Cancel and unregister
        e.setCancelled(true);
        unregister();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (e.getPlayer() != player) {
            return;
        }
        unregister();
    }

    public BukkitTask start(long delay, long period) {
        this.register();
        return this.runTaskTimerAsynchronously(plugin, delay, period);
    }

    private void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void unregister() {
        HandlerList.unregisterAll(this);
        this.cancel();
    }

}
