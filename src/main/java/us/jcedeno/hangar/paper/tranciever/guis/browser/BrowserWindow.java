package us.jcedeno.hangar.paper.tranciever.guis.browser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;

import fr.mrmicky.fastinv.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.tranciever.RapidInv;
import us.jcedeno.hangar.paper.tranciever.guis.creator.CreatorGUI;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.GameType;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.UHCData;
import us.jcedeno.hangar.paper.tranciever.guis.tranceiver.RecieverGUI;
import us.jcedeno.hangar.paper.tranciever.utils.ServerData;
import us.jcedeno.hangar.paper.tranciever.utils.SlotPos;

public class BrowserWindow extends RapidInv {

    private @Getter static ArrayList<Integer> addresablesIndexes = obtainIndexes();
    public static int slot_browser_icon = SlotPos.from(4, 0);
    public static int slot_game_creator = SlotPos.from(2, 5);
    public static int slot_home_button = SlotPos.from(4, 5);
    public static int slot_private_games = SlotPos.from(6, 5);
    private CreatorGUI creatorGUI;
    private @Getter @Setter GameType currentType;
    private @Getter Set<ServerData> lastKnownData;
    private Hangar instance;
    private @Getter boolean privacy = false;

    public BrowserWindow(GameType type, RapidInv parentInventory, Hangar instance) {
        super(9 * 6, type.toString() + " Browser");
        this.instance = instance;
        if (parentInventory != null)
            setParentInventory(parentInventory);
        // Set the type
        setCurrentType(type);
        setItem(slot_browser_icon, type.getBrowserIcon(), (e) -> nextBrowser(e, type, instance));
        setItem(slot_game_creator, new ItemBuilder(Material.IRON_PICKAXE).flags(ItemFlag.HIDE_ATTRIBUTES)
                .name(ChatColor.of("#f49348") + "Game Creator").build(), (e) -> {
                    getCreator(getCurrentType(), instance, e.getWhoClicked());
                    var clickedPlayer = (Player) e.getWhoClicked();
                    clickedPlayer.playSound(clickedPlayer.getLocation(), Sound.ENTITY_SHULKER_AMBIENT,
                            SoundCategory.VOICE, 1.0f, 1.0f);
                });
        setItem(slot_home_button,
                new ItemBuilder(Material.WARPED_DOOR).name(ChatColor.of("#918bf8") + "Main Menu").build(), (e) -> {
                    var inv = getParentInventory();
                    while (!(inv instanceof RecieverGUI)) {
                        inv = inv.getParentInventory();
                    }
                    if (inv != null) {
                        inv.open(e.getWhoClicked());
                    }
                });
        setItem(slot_private_games,
                new ItemBuilder(Material.LODESTONE).name(ChatColor.of("#f49348") + "Private Games").build(),
                (e) -> new BrowserWindow(this, instance, privacy).open(e.getWhoClicked(), instance));

        update(instance.getCommunicatorManager().getCachedData());
    }

    public BrowserWindow(RapidInv parentInventory, Hangar instance, boolean privacy) {
        super(9 * 6, "Private Matches");
        this.instance = instance;
        this.privacy = true;
        if (parentInventory != null)
            setParentInventory(parentInventory);
        setItem(slot_browser_icon, new ItemBuilder(Material.TURTLE_HELMET).name(ChatColor.YELLOW + "Private Games")
                .flags(ItemFlag.HIDE_ATTRIBUTES).build(), (e) -> {
                    // TODO: ON CLICK BEHAVIOR
                });
        setItem(slot_home_button,
                new ItemBuilder(Material.WARPED_DOOR).name(ChatColor.of("#918bf8") + "Main Menu").build(), (e) -> {
                    var inv = getParentInventory();
                    while (!(inv instanceof RecieverGUI)) {
                        inv = inv.getParentInventory();
                    }
                    if (inv != null) {
                        inv.open(e.getWhoClicked());
                    }
                });

        updatePrivateGames(instance.getCommunicatorManager().getCachedData());
    }

    private void nextBrowser(InventoryClickEvent e, GameType type, Hangar instance) {
        new BrowserWindow(e.getClick() == ClickType.RIGHT ? type.getNextType() : type.getPreviousType(), this, instance)
                .open(e.getWhoClicked(), instance);
        var clickedPlayer = (Player) e.getWhoClicked();
        clickedPlayer.playSound(clickedPlayer.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE, SoundCategory.VOICE, 1.0f,
                1.0f);

    }

    public void updatePrivateGames(Set<ServerData> updateData) {

        var managedData = new HashSet<>(updateData);
        managedData.removeIf(all -> all == null || all.getGameType() == null || !all.isPrivate_game());

        if (managedData.isEmpty()) {
            addresablesIndexes.forEach(slot -> removeItem(slot));
            var first_index = addresablesIndexes.get(0);
            setItem(first_index, GameType.UHC.getDefaultItem(), e -> {
                var clickedPlayer = (Player) e.getWhoClicked();
                clickedPlayer.playSound(clickedPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,
                        SoundCategory.VOICE, 1.0f, 1.0f);
            });

        } else {
            var indexIterator = addresablesIndexes.iterator();
            managedData.forEach(all -> {
                if (indexIterator.hasNext()) {
                    var index = indexIterator.next();
                    var gson = new Gson();
                    var data = gson.fromJson(all.getExtra_data().get("uhc-data").toString(), UHCData.class);
                    updateItem(index, all.getGameType().asServerDataIcon(data), e -> {
                        instance.getCommunicatorManager().sendToIP((Player) e.getWhoClicked(), all.getIpv4(),
                                all.getGame_id().toString());
                    });
                } else {

                }
            });
            while (indexIterator.hasNext()) {
                removeItem(indexIterator.next());
            }

        }
    }

    public void update(Set<ServerData> updateData) {
        // Just keep the data tha is related to our window.
        if (isPrivacy()) {
            updatePrivateGames(updateData);
            return;
        }
        var managedData = new ArrayList<>(updateData);
        managedData.removeIf(
                all -> (all.getGameType() == null || all.getGameType() != getCurrentType() || all.isPrivate_game()));

        // TODO: SORT THE DATA TO BE DISPLAYED
        // TODO: PAGINATION SYSTEM

        if (managedData.isEmpty()) {
            addresablesIndexes.forEach(slot -> removeItem(slot));
            var first_index = addresablesIndexes.get(0);
            setItem(first_index, getCurrentType().getDefaultItem(), e -> {
                var clickedPlayer = (Player) e.getWhoClicked();
                clickedPlayer.playSound(clickedPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,
                        SoundCategory.VOICE, 1.0f, 1.0f);
            });

        } else {
            /* Attempt to sort by online players */
            Collections.sort(managedData, Comparator.comparingInt(ServerData::getPlayersIn));

            var indexIterator = addresablesIndexes.iterator();
            managedData.forEach(all -> {
                if (indexIterator.hasNext()) {
                    var index = indexIterator.next();
                    var gson = new Gson();
                    var data = gson.fromJson(all.getExtra_data().get("uhc-data").toString(), UHCData.class);
                    updateItem(index, all.getGameType().asServerDataIcon(data), e -> {
                        instance.getCommunicatorManager().sendToIP((Player) e.getWhoClicked(), all.getIpv4(),
                                all.getGame_id().toString());
                    });
                } else {

                }
            });
            while (indexIterator.hasNext()) {
                removeItem(indexIterator.next());
            }

        }
    }

    public CreatorGUI getCreator(GameType type, Hangar plugin, HumanEntity humanEntity) {
        if (creatorGUI == null)
            creatorGUI = new CreatorGUI(type.toString() + " Creator", this, plugin, type, humanEntity);

        creatorGUI.open(humanEntity);
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
