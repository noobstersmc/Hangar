package us.jcedeno.hangar.paper.tranciever.guis.tranceiver;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import fr.mrmicky.fastinv.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.communicator.LoreBuilder;
import us.jcedeno.hangar.paper.tranciever.RapidInv;
import us.jcedeno.hangar.paper.tranciever.guis.browser.BrowserWindow;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.GameType;
import us.jcedeno.hangar.paper.tranciever.utils.ServerData;
import us.jcedeno.hangar.paper.tranciever.utils.SlotPos;

public class RecieverGUI extends RapidInv {
    public RecieverGUI(String title, Hangar instance, Player player) {
        super(9 * 4, title);
        this.setItem(SlotPos.from(2, 1),
                new ItemBuilder(Material.ENCHANTED_GOLDEN_APPLE).name(ChatColor.of("#f64658") + "" + ChatColor.BOLD + "UHC").build(), (e) -> {
                    new BrowserWindow(GameType.UHC, this, instance).open(e.getWhoClicked());
                });
        this.setItem(SlotPos.from(4, 1), new ItemBuilder(Material.APPLE).name(ChatColor.YELLOW + "" + ChatColor.BOLD + "UHC Run")
                .enchant(Enchantment.ARROW_DAMAGE).flags(ItemFlag.HIDE_ENCHANTS).build(), (e) -> {
                    new BrowserWindow(GameType.RUN, this, instance).open(e.getWhoClicked());
                });
        this.setItem(SlotPos.from(6, 1), new ItemBuilder(Material.CROSSBOW).name(ChatColor.GREEN + "" + ChatColor.BOLD + "UHC Meetup").build(),
                (e) -> {
                    new BrowserWindow(GameType.MEETUP, this, instance).open(e.getWhoClicked());
                });

        var head = getPlayerHead(player, ChatColor.of("#a1f448") + player.getName() + "'s profile");
        head.setLore(LoreBuilder.of(ChatColor.WHITE + "Coming soon..."));

        this.setItem(SlotPos.from(4, 3), head);
        update(instance.getCommunicatorManager().getCachedData());

    }

    public void update(Set<ServerData> data) {
        var uhc_count = 0;
        var uhc_server = 0;
        var run_count = 0;
        var run_server = 0;
        var meetup_count = 0;
        var meetup_server = 0;
        

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
                    case MEETUP:
                        meetup_count += uhcData.getPlayersOnline();
                        meetup_server++;
    
                        break;
    
                    default:
                        break;
                }
                
            } catch (Exception e) {
                //TODO: handle exception
            }

        }
        
        getInventory().getItem(SlotPos.from(2, 1)).setLore(LoreBuilder.of(ChatColor.of("#b4889a") + "Players: " + ChatColor.WHITE + uhc_count, ChatColor.of("#b4889a") + "Servers: " + ChatColor.WHITE + uhc_server));
        getInventory().getItem(SlotPos.from(4, 1)).setLore(LoreBuilder.of(ChatColor.of("#b4889a") + "Players: " + ChatColor.WHITE + run_count, ChatColor.of("#b4889a") + "Servers: " + ChatColor.WHITE + run_server));
        getInventory().getItem(SlotPos.from(6, 1)).setLore(LoreBuilder.of(ChatColor.of("#b4889a") + "Players: " + ChatColor.WHITE + meetup_count, ChatColor.of("#b4889a") + "Servers: " + ChatColor.WHITE + meetup_server));


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
