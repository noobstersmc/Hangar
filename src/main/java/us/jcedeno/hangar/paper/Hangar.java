package us.jcedeno.hangar.paper;

import com.google.gson.Gson;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitWorker;

import co.aikar.commands.PaperCommandManager;
import fr.mrmicky.fastinv.FastInvManager;
import lombok.Getter;
import lombok.Setter;
import us.jcedeno.hangar.paper.chat.ChatManager;
import us.jcedeno.hangar.paper.chat.LPManager;
import us.jcedeno.hangar.paper.commands.DebugCMD;
import us.jcedeno.hangar.paper.commands.SlotCMD;
import us.jcedeno.hangar.paper.communicator.CommunicatorManager;
import us.jcedeno.hangar.paper.condor.CondorManager;
import us.jcedeno.hangar.paper.scoreboard.ScoreboardManager;
import us.jcedeno.hangar.paper.tranciever.RapidInvManager;

public class Hangar extends JavaPlugin {
    private @Getter PaperCommandManager commandManager;
    private @Getter ScoreboardManager scoreboardManager;
    private @Getter ChatManager chatManager;
    private @Getter CommunicatorManager communicatorManager;
    private @Getter Arena arena;
    private @Getter GlobalListeners globalListeners;
    private @Getter Decoration decoration;
    private @Getter LPManager lpManager;
    private @Getter CondorManager condorManager;
    private @Getter @Setter int maxSlots = 100;

    // GUI tutorial: https://github.com/MrMicky-FR/FastInv
    // Scoreboard Tutorial: https://github.com/MrMicky-FR/FastBoard
    // Commands Tutorial: https://github.com/aikar/commands/wiki/Using-ACF
    @Override
    public void onEnable() {
        FastInvManager.register(this);
        RapidInvManager.register(this);

        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            this.chatManager = new ChatManager(this);
        } else {
            getLogger().info("Couldn't setup chat. Vault is missing.");
        }

        this.scoreboardManager = new ScoreboardManager(this);
        this.commandManager = new PaperCommandManager(this);
        this.commandManager.registerCommand(new SlotCMD(this));
        this.communicatorManager = new CommunicatorManager(this);
        this.lpManager = new LPManager(this);
        this.arena = new Arena(this);
        this.globalListeners = new GlobalListeners(this);
        this.decoration = new Decoration(this);
        Bukkit.getOnlinePlayers().forEach(all -> {
            scoreboardManager.sendInitialBoard(all);
        });
        var gson = new Gson();
        arena.loadRestoreTaks(gson, Bukkit.getConsoleSender());
        arena.loadPlayerData(gson, Bukkit.getConsoleSender());
        this.condorManager = new CondorManager(this);

        this.commandManager.registerCommand(new DebugCMD(this));

    }

    @Override
    public void onDisable() {
        var gson = new Gson();
        arena.saveRestoreTaks(gson, Bukkit.getConsoleSender());
        arena.savePlayerData(gson, Bukkit.getConsoleSender());
        getServer().getScheduler().getActiveWorkers().stream().filter(w -> w.getOwner() == this)
                .map(BukkitWorker::getThread).forEach(Thread::interrupt);
        getServer().getScheduler().cancelTasks(this);
        
        if (communicatorManager.getJedis().isConnected())
            communicatorManager.getJedis().disconnect();

    }

}
