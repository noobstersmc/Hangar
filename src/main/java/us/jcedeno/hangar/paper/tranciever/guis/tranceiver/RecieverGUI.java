package us.jcedeno.hangar.paper.tranciever.guis.tranceiver;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import fr.mrmicky.fastinv.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.communicator.LoreBuilder;
import us.jcedeno.hangar.paper.tranciever.RapidInv;
import us.jcedeno.hangar.paper.tranciever.guis.browser.BrowserWindow;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.GameType;
import us.jcedeno.hangar.paper.tranciever.utils.SlotPos;

public class RecieverGUI extends RapidInv {

    public RecieverGUI(String title, Plugin instance) {
        super(9 * 4, title);
        // TODO: UPDATE ITEMS WITH DATA FOR EACH CATEGORY [PLAYERS AND MATCHES]
        this.setItem(SlotPos.from(1, 1),
                new ItemBuilder(Material.ENCHANTED_GOLDEN_APPLE).name(ChatColor.GOLD + "UHC").build(), (e) -> {
                    new BrowserWindow(GameType.UHC, this, instance).open(e.getWhoClicked());
                });
        this.setItem(SlotPos.from(3, 1), new ItemBuilder(Material.APPLE).name(ChatColor.YELLOW + "UHC Run")
                .enchant(Enchantment.ARROW_DAMAGE).flags(ItemFlag.HIDE_ENCHANTS).build(), (e) -> {
                    e.getWhoClicked().sendMessage("Uhc Run");
                    new BrowserWindow(GameType.RUN, this, instance).open(e.getWhoClicked());
                });
        this.setItem(SlotPos.from(5, 1), new ItemBuilder(Material.CROSSBOW).name(ChatColor.AQUA + "UHC Meetup").build(),
                (e) -> {
                    e.getWhoClicked().sendMessage("Meetup");
                    new BrowserWindow(GameType.MEETUP, this, instance).open(e.getWhoClicked());
                });
        this.setItem(SlotPos.from(7, 1),
                new ItemBuilder(Material.CAKE).name(ChatColor.LIGHT_PURPLE + "Community Games").build(), (e) -> {
                    e.getWhoClicked().sendMessage("Community");
                });
    }

    public void openChildren(Player player) {
        var individualGUI = clone("Tranceiver for " + player.getName());

        var head = getPlayerHead(player, ChatColor.GOLD + player.getName() + "'s stats");
        head.setLore(LoreBuilder.of("Coming soon."));

        individualGUI.setItem(SlotPos.from(4, 3), head, e -> {
            e.getWhoClicked().sendMessage("This feature is not ready yet.");
        });

        individualGUI.open(player);

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
