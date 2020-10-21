package us.jcedeno.hangar.paper;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitWorker;

import co.aikar.commands.PaperCommandManager;
import fr.mrmicky.fastinv.FastInvManager;
import lombok.Getter;
import us.jcedeno.hangar.paper.chat.ChatManager;
import us.jcedeno.hangar.paper.chat.LPManager;
import us.jcedeno.hangar.paper.communicator.CommunicatorManager;
import us.jcedeno.hangar.paper.scoreboard.ScoreboardManager;

public class Hangar extends JavaPlugin {
    private @Getter PaperCommandManager commandManager;
    private @Getter ScoreboardManager scoreboardManager;
    private @Getter ChatManager chatManager;
    private @Getter CommunicatorManager communicatorManager;
    private @Getter Arena arena;
    private @Getter GlobalListeners globalListeners;
    private @Getter LPManager lpManager;

    // GUI tutorial: https://github.com/MrMicky-FR/FastInv
    // Scoreboard Tutorial: https://github.com/MrMicky-FR/FastBoard
    // Commands Tutorial: https://github.com/aikar/commands/wiki/Using-ACF
    @Override
    public void onEnable() {
        FastInvManager.register(this);

        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            this.chatManager = new ChatManager(this);
        } else {
            getLogger().info("Couldn't setup chat. Vault is missing.");
        }

        this.scoreboardManager = new ScoreboardManager(this);
        this.commandManager = new PaperCommandManager(this);
        this.communicatorManager = new CommunicatorManager(this);
        this.lpManager = new LPManager(this);
        this.arena = new Arena(this);
        this.globalListeners = new GlobalListeners(this);
        Bukkit.getOnlinePlayers().forEach(all->{
            scoreboardManager.sendInitialBoard(all);
        });

    }

    @Override
    public void onDisable() {
        getServer().getScheduler().getActiveWorkers().stream().filter(w -> w.getOwner() == this)
                .map(BukkitWorker::getThread).forEach(Thread::interrupt);
        getServer().getScheduler().cancelTasks(this);

    }

}
