package us.jcedeno.hangar.paper.tranciever.guis.browser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import fr.mrmicky.fastinv.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.tranciever.RapidInv;
import us.jcedeno.hangar.paper.tranciever.guis.creator.CreatorGUI;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.GameType;
import us.jcedeno.hangar.paper.tranciever.utils.SlotPos;
import us.jcedeno.hangar.paper.uhc.GameData;

public class BrowserWindow extends RapidInv {

    private @Getter static ArrayList<Integer> addresablesIndexes = obtainIndexes();
    public static int slot_browser_icon = SlotPos.from(4, 0);
    public static int slot_game_creator = SlotPos.from(2, 5);
    public static int slot_home_button = SlotPos.from(4, 5);
    public static int slot_private_games = SlotPos.from(6, 5);
    private CreatorGUI creatorGUI;
    private @Getter @Setter GameType currentType;
    private Set<GameData> lastKnownData;

    public BrowserWindow(GameType type, RapidInv parentInventory, Plugin instance) {
        super(9 * 6, type.toString() + " Browser");
        if (parentInventory != null)
            setParentInventory(parentInventory);
        // Set the type
        setCurrentType(type);
        setItem(slot_browser_icon, type.getBrowserIcon(),
                // TODO: HANDLE THE CLICK AND CHANGE BROWSER BASED ON CLICK
                (e) -> {
                    setCurrentType(getCurrentType().getNextType());
                    updateItem(slot_browser_icon, currentType.getBrowserIcon(), predicado -> {
                        e.getWhoClicked().sendMessage("Switching to " + type.getNextType().toString());
                    });
                    e.getWhoClicked().sendMessage("Switched to " + type.getNextType().toString());
                    update(lastKnownData);

                });
        setItem(slot_game_creator, new ItemBuilder(Material.IRON_PICKAXE).flags(ItemFlag.HIDE_ATTRIBUTES)
                .name(ChatColor.GOLD + "Game Creator").build(), (e) -> {
                    getCreator(type, instance).open(e.getWhoClicked());
                });
        setItem(slot_home_button, new ItemBuilder(Material.ACACIA_DOOR).name(ChatColor.GOLD + "Home").build(), (e) -> {
            if (getParentInventory() != null) {
                getParentInventory().open(e.getWhoClicked());
            }
        });
        setItem(slot_private_games, new ItemBuilder(Material.LODESTONE).name(ChatColor.GOLD + "Private Games").build(),
                (e) -> e.getWhoClicked().sendMessage("Private games are not ready yet!"));

        update(Collections.emptySet());
    }

    public void update(Set<GameData> updateData) {
        // Just keep the data tha is related to our window.
        updateData.removeIf(data -> data.getGameType() != getCurrentType().toString());
        if (updateData.isEmpty()) {
            addresablesIndexes.forEach(slot -> updateItem(slot, new ItemStack(Material.AIR), e -> {
            }));
            var first_index = addresablesIndexes.get(0);
            updateItem(first_index, getCurrentType().getDefaultItem(),
                    e -> e.getWhoClicked().sendMessage("No games running"));

        } else {
            // Is there is data, then check latest data and update. Also handle pagination
            // here.

        }

        // Update the last known data
        lastKnownData = updateData;
    }

    public CreatorGUI getCreator(Plugin plugin) {
        return getCreator(GameType.UHC, plugin);
    }

    public CreatorGUI getCreator(GameType type, Plugin plugin) {
        if (creatorGUI == null)
            creatorGUI = new CreatorGUI(type.toString() + " Creator", this, plugin, type);

        return creatorGUI;
    }

    private static ArrayList<Integer> obtainIndexes() {
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
        return matches;
    }

}
