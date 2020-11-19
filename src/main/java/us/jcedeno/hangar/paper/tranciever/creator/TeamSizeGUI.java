package us.jcedeno.hangar.paper.tranciever.creator;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;

import fr.mrmicky.fastinv.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.communicator.LoreBuilder;
import us.jcedeno.hangar.paper.tranciever.RapidInv;

public class TeamSizeGUI extends RapidInv {
    private Player player;
    private CreatorGUI creator;
    private Integer size = 1;

    public TeamSizeGUI(Player player, CreatorGUI creator, String title) {
        super(InventoryType.HOPPER, title);
        this.player = player;
        this.creator = creator;

        setItem(2, getPlayerHead(player, "Team Size:"), e -> {
            System.out.println("Team size " + size);
            creator.getInventory().getItem(creator.slot_for_teamsize).setLore(LoreBuilder.of(ChatColor.WHITE + (size > 1 ? "To" + size : "FFA")));
            creator.open(player);
        });

        // Add substact items
        var less_item = new ItemBuilder(Material.TIPPED_ARROW).flags(ItemFlag.HIDE_POTION_EFFECTS)
                .name(ChatColor.WHITE + "Minus (-)").build();
        var meta = (PotionMeta) less_item.getItemMeta();
        meta.setColor(Color.fromRGB(240, 231, 74));
        less_item.setItemMeta(meta);

        setItem(0, less_item, e -> {
            size--;
            getInventory().getItem(2).setAmount(size);
            getInventory().getItem(2).setLore(LoreBuilder.of("" + size));
        });

        var plus_item = new ItemBuilder(Material.TIPPED_ARROW).flags(ItemFlag.HIDE_POTION_EFFECTS)
                .name(ChatColor.WHITE + "Plus (+)").build();
        var plus_meta = (PotionMeta) plus_item.getItemMeta();
        plus_meta.setColor(Color.fromRGB(81, 219, 111));
        plus_item.setItemMeta(plus_meta);

        setItem(4, plus_item, e -> {
            size++;
            getInventory().getItem(2).setAmount(size);
            getInventory().getItem(2).setLore(LoreBuilder.of("" + size));
        });
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
