package us.jcedeno.hangar.paper.tranciever.guis.creator.objects;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import lombok.Getter;
import us.jcedeno.hangar.paper.condor.NewCondor;

public class Token {
    private @Getter ArrayList<String> stringArray = new ArrayList<>();
    private int index = 0;
    private Player player;

    public Token(Player player) {
        this.player = player;
        if (player.hasPermission("noobsters.bill")) {
            stringArray.add("Noobsters");
        }
        if (player.hasPermission("group.vandal")) {
            stringArray.add("Vandal");
        }
        if (player.hasPermission("group.quadrados")) {
            stringArray.add("Quadrados");
        }
        stringArray.add(player.getName());
    }

    boolean hasPermission(String str) {
        return true;
    }

    public String next() {
        index++;
        if (index > (stringArray.size() - 1)) {
            index = 0;
            return stringArray.get(index);

        } else {
            return stringArray.get(index);
        }
    }

    public String current() {
        return stringArray.get(index);
    }

    public String currentTokenKey() {
        var current = current();
        if (current.equalsIgnoreCase("Noobsters")) {
            return "6QR3W05K3F";
        } else if (current.equalsIgnoreCase("Vandal")) {
            return "rZhZNPiWSo";
        } else if (current.equalsIgnoreCase("Quadrados")) {
            return "X2XWdMMoQH";
        } else {
            return NewCondor.getTokenMap().getOrDefault(player.getUniqueId().toString(),
                    player.getUniqueId().toString());
        }
    }

}
