package us.jcedeno.hangar.paper.chat;

import org.bukkit.Bukkit;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.scoreboard.ScoreboardManager;

public class LPManager {
    private Hangar instance;
    private ScoreboardManager scoreboardManager;

    public LPManager(Hangar instance) {
        this.instance = instance;
        this.scoreboardManager = instance.getScoreboardManager();

        var plugin = Bukkit.getPluginManager().getPlugin("LuckPerms");

        if (plugin != null) {
            var lp = LuckPermsProvider.get();
            instance.getLogger().info("Hooking to LP");
            var bus = lp.getEventBus();
            bus.subscribe(NodeAddEvent.class, e -> {
                if (e.getNode().getType() == NodeType.INHERITANCE && e.isUser()) {
                    if (e.getTarget() instanceof User) {
                        var user = (User) e.getTarget();
                        var player = Bukkit.getPlayer(user.getUniqueId());
                        if (player == null || !player.isOnline())
                            return;

                        var board = scoreboardManager.getBoards().get(user.getUniqueId());
                        if (board != null) {
                            board.updateLine(4, " " + scoreboardManager.getGroup(player));
                        }
                    }
                }
            });

        } else {
            instance.getLogger().info("Couldn't hook to luckperms.");
        }
    }

}
