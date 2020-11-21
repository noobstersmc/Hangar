package us.jcedeno.hangar.paper.tranciever.guis.browser;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import fr.mrmicky.fastinv.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.tranciever.RapidInv;
import us.jcedeno.hangar.paper.tranciever.guis.creator.CreatorGUI;
import us.jcedeno.hangar.paper.tranciever.utils.SlotPos;

public class UHCGui extends RapidInv {

    public UHCGui(String title, Hangar instance) {
        super(54, title);
        setItem(SlotPos.from(4, 0), new ItemBuilder(Material.NETHERITE_HELMET).flags(ItemFlag.HIDE_ATTRIBUTES)
                .name(ChatColor.GOLD + "Official Games").lore(ChatColor.WHITE + "Click to change game type.").build(),
                (e) -> e.getWhoClicked().sendMessage("Switching to UHC Run"));
        setItem(SlotPos.from(2, 5), new ItemBuilder(Material.IRON_PICKAXE).flags(ItemFlag.HIDE_ATTRIBUTES)
                .name(ChatColor.GOLD + "Game Creator").build(), (e) -> {
                    var gameCreatorWindow = new CreatorGUI("Creator", instance, this);
                    gameCreatorWindow.open(((Player) e.getWhoClicked()));
                });
        setItem(SlotPos.from(4, 5), new ItemBuilder(Material.ACACIA_DOOR).name(ChatColor.GOLD + "Home").build(),
                (e) -> ((Player) e.getWhoClicked()).performCommand("debug"));
        setItem(SlotPos.from(6, 5), new ItemBuilder(Material.LODESTONE).name(ChatColor.GOLD + "Private Games").build(),
                (e) -> e.getWhoClicked().sendMessage("Displaying Private Games"));
        // Obtain all the possible slots.
        var matches = new ArrayList<Integer>();
        int range_1_start = SlotPos.from(1, 1);
        int range_1_end = SlotPos.from(7, 1);
        while (range_1_start <= range_1_end) {
            matches.add(range_1_start++);
        }
        int range_2_start = SlotPos.from(1, 2);
        int range_2_end = SlotPos.from(7, 2);
        while (range_2_start <= range_2_end) {
            matches.add(range_2_start++);
        }
        int range_3_start = SlotPos.from(1, 3);
        int range_3_end = SlotPos.from(7, 3);
        while (range_3_start <= range_3_end) {
            matches.add(range_3_start++);
        }
        var iterator = matches.iterator();
        while (iterator.hasNext()) {
            var next = iterator.next();
            if (!iterator.hasNext()) {
                setItem(next, new ItemBuilder(Material.SPECTRAL_ARROW).name(ChatColor.WHITE + "Next Page").build());
                break;
            } else {
                setItem(next, new ItemBuilder(Material.ENCHANTED_GOLDEN_APPLE).name(ChatColor.YELLOW + "Dummy UHC")
                        .lore("no data").build());
            }

        }
    }

}
