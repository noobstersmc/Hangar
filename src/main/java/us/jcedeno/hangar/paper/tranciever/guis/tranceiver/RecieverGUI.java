package us.jcedeno.hangar.paper.tranciever.guis.tranceiver;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import com.google.gson.JsonObject;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import fr.mrmicky.fastinv.ItemBuilder;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.communicator.LoreBuilder;
import us.jcedeno.hangar.paper.tranciever.RapidInv;
import us.jcedeno.hangar.paper.tranciever.guis.browser.BrowserWindow;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.GameType;
import us.jcedeno.hangar.paper.tranciever.utils.ServerData;
import us.jcedeno.hangar.paper.tranciever.utils.SlotPos;

public class RecieverGUI extends RapidInv {
    private @Getter UUID uuid;

    public RecieverGUI(String title, Hangar instance, Player player, JsonObject profile) {
        super(9 * 4, title);
        this.uuid = player.getUniqueId();

        this.setItem(SlotPos.from(2, 1), new ItemBuilder(Material.ENCHANTED_GOLDEN_APPLE)
                .name(ChatColor.of("#f64658") + "" + ChatColor.BOLD + "UHC").build(), (e) -> {
                    new BrowserWindow(GameType.UHC, this, instance).open(e.getWhoClicked());
                    var clickedPlayer = (Player) e.getWhoClicked();
                    clickedPlayer.playSound(clickedPlayer.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE,
                            SoundCategory.VOICE, 1.0f, 1.0f);
                });

        this.setItem(SlotPos.from(6, 1),
                new ItemBuilder(Material.APPLE).name(ChatColor.YELLOW + "" + ChatColor.BOLD + "UHC Run")
                        .enchant(Enchantment.ARROW_DAMAGE).flags(ItemFlag.HIDE_ENCHANTS).build(),
                (e) -> {
                    new BrowserWindow(GameType.RUN, this, instance).open(e.getWhoClicked());
                    var clickedPlayer = (Player) e.getWhoClicked();
                    clickedPlayer.playSound(clickedPlayer.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE,
                            SoundCategory.VOICE, 1.0f, 1.0f);
                });

        /*this.setItem(SlotPos.from(5, 1),
                new ItemBuilder(Material.EMERALD).name(ChatColor.GREEN + "" + ChatColor.BOLD + "UHC Meetup")
                        .enchant(Enchantment.ARROW_DAMAGE).flags(ItemFlag.HIDE_ENCHANTS).build(),
                (e) -> {
                    new BrowserWindow(GameType.MEETUP, this, instance).open(e.getWhoClicked());
                    var clickedPlayer = (Player) e.getWhoClicked();
                    clickedPlayer.playSound(clickedPlayer.getLocation(), Sound.ITEM_ARMOR_EQUIP_TURTLE,
                            SoundCategory.VOICE, 1.0f, 1.0f);
                });*/

        /*var survival_item = new ItemBuilder(Material.GRASS_BLOCK)
                .name(ChatColor.AQUA + "" + ChatColor.BOLD + "Survival Nightmare")
                .lore(ChatColor.of("#c7c7c7") + "A combination between Survival, Anarchy,",
                ChatColor.of("#c7c7c7") + "PvP and Permadeath difficulty changes.",
                ChatColor.of("#a1b2cc") + "Only special users!").build();


        this.setItem(SlotPos.from(7, 1), survival_item, (e) -> instance.getCommunicatorManager()
                .sendToIP((Player) e.getWhoClicked(), "localhost:25579", "00fa1b59-cfa0-4ce5-9f80-7c30f04d1610"));*/

        var head = getPlayerHead(player, ChatColor.of("#a1f448") + player.getName() + "'s profile");
        head.setLore(LoreBuilder.of(ChatColor.WHITE + "Coming soon..."));

        this.setItem(SlotPos.from(4, 3), head);
        update(instance.getCommunicatorManager().getCachedData(), profile);
    }

    @Override
    public <T extends HumanEntity> void open(T player) {
        super.open(player);
    }

    public void update(Set<ServerData> data, JsonObject profile) {
        var uhc_count = 0;
        var uhc_server = 0;
        var run_count = 0;
        var run_server = 0;
        //var meetup_count = 0;
        //var meetup_server = 0;

        for (ServerData serverData : data) {
            try {
                var uhcData = serverData.getUhcData();
                switch (serverData.getGameType()) {
                    case UHC:
                        uhc_count += uhcData.getPlayersOnline();
                        uhc_server++;

                        break;
                    case RUN:
                        run_count += uhcData.getPlayersOnline();
                        run_server++;
                        break;

                    /*case MEETUP:
                        meetup_count += uhcData.getPlayersOnline();
                        meetup_server++;

                        break;*/

                    default:
                        break;
                }

            } catch (Exception e) {
                // TODO: handle exception
            }

        }

        getInventory().getItem(SlotPos.from(2, 1))
                .setLore(LoreBuilder.of(ChatColor.of("#c7c7c7") + "Players: " + ChatColor.WHITE + uhc_count,
                        ChatColor.of("#c7c7c7") + "Servers: " + ChatColor.WHITE + uhc_server));

        getInventory().getItem(SlotPos.from(6, 1))
                .setLore(LoreBuilder.of(ChatColor.of("#c7c7c7") + "Players: " + ChatColor.WHITE + run_count,
                        ChatColor.of("#c7c7c7") + "Servers: " + ChatColor.WHITE + run_server));

        /*getInventory().getItem(SlotPos.from(5, 1))
                .setLore(LoreBuilder.of(ChatColor.of("#c7c7c7") + "Players: " + ChatColor.WHITE + meetup_count,
                        ChatColor.of("#c7c7c7") + "Servers: " + ChatColor.WHITE + meetup_server));*/

        if (profile == null) {
            getInventory().getItem(SlotPos.from(4, 3))
                    .setLore(LoreBuilder.of(ChatColor.WHITE + "Community Host: " + ChatColor.RED + "Inactive"));
        } else {
            var lore = new ArrayList<String>();
            lore.add(ChatColor.WHITE + "Host Token: " + ChatColor.GREEN + profile.get("name").getAsString());
            var credits = profile.get("credits").getAsString();
            lore.add(ChatColor.WHITE + "Credits: " + ChatColor.GOLD
                    + (credits.equalsIgnoreCase("-420") ? "Unlimited" : credits));
            var instances = profile.getAsJsonArray("instances");
            if (instances != null) {
                var iter = instances.iterator();
                var count = 0;
                while (iter.hasNext()) {
                    iter.next();
                    count++;
                }
                lore.add(ChatColor.WHITE + "Active Games: " + ChatColor.GREEN + count);
            }

            getInventory().getItem(SlotPos.from(4, 3)).setLore(lore);

        }

    }

    private ItemStack getPlayerHead(Player player, String name) {
        var player_head = new ItemStack(Material.PLAYER_HEAD);
        var skull = (SkullMeta) player_head.getItemMeta();
        skull.setOwningPlayer(player);
        skull.setDisplayName(name);
        player_head.setItemMeta(skull);
        return player_head;
    }
}
