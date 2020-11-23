package us.jcedeno.hangar.paper.scoreboard;

import java.awt.Color;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.mrmicky.fastboard.FastBoard;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.chat.ChatManager;

public class ScoreboardManager {
    private Hangar instance;
    private @Getter HashMap<UUID, FastBoard> boards = new HashMap<>();

    public ScoreboardManager(Hangar instance) {
        this.instance = instance;
    }

    /**
     * @param player Player to receieve the scoreboard update
     * @param title  Optional title, required if the player has no scoreboard.
     * @param lines  Array of lines to be updated or created.
     */
    public void updatePlayerBoard(Player player, String title, String... lines) {
        // Get or create a new board
        var fb = boards.getOrDefault(player.getUniqueId(), new FastBoard(player));
        // Update the title if necessary.
        if (!title.isEmpty())
            fb.updateTitle(title);
        // Update the lines

        fb.updateLines(lines);

        // If the player had no board, add it to the map.
        boards.putIfAbsent(player.getUniqueId(), fb);

    }

    /**
     * @param uuid  Player to receieve the scoreboard update
     * @param title Optional title, required if the player has no scoreboard.
     * @param lines Array of lines to be updated or created.
     */
    public void updatePlayerBoard(UUID uuid, String title, String... lines) {
        // Get or create a new board
        var fb = boards.getOrDefault(uuid, new FastBoard(Bukkit.getPlayer(uuid)));
        // Update the title if necessary.
        if (!title.isEmpty())
            fb.updateTitle(title);
        // Update the lines
        fb.updateLines(lines);
        // If the player had no board, add it to the map.
        boards.putIfAbsent(uuid, fb);
    }

    public void sendInitialBoard(Player player) {
        // Get or create a new board
        var fb = boards.getOrDefault(player.getUniqueId(), new FastBoard(player));
        fb.updateTitle(ChatColor.of("#A40A0A") + "" + ChatColor.BOLD + "NOOBSTERS");
        // Update the lines
        fb.updateLines(ChatColor.of("#2be49c") + "User:",
                " " + ChatColor.WHITE + player.getName() + "         \uEff3", "", ChatColor.of("#2be49c") + "Role: ",
                ChatColor.WHITE + " " + ChatManager.colorize(getGroup(player)), "",
                ChatColor.of("#2be49c") + "Online Players: ",
                " " + ChatColor.WHITE + instance.getCommunicatorManager().getProxyPlayers(), "",
                ChatColor.WHITE + "noobsters.net");         

        boards.putIfAbsent(player.getUniqueId(), fb);
    }

    public String getGroup(final Player player) {
        var group = instance.getChatManager().getPerms().getPrimaryGroup(player);
        var first = group.charAt(0);
        return (first + "").toUpperCase() + instance.getChatManager().getPerms().getPrimaryGroup(player).substring(1);
    }

    public static void main(String[] args) {
        for (float i = 0; i < 1.0f; i += 0.01f) {

            var color = Color.getHSBColor(i, 1.0f, 1.0f);
            var coloronhex = String.format("#%06X", (0xFFFFFF & color.getRGB()));
            System.out.println(coloronhex);
        }
    }

    public static void rgb() {
    }

    public FastBoard getBoard(UUID uuid) {
        return boards.get(uuid);
    }

}
