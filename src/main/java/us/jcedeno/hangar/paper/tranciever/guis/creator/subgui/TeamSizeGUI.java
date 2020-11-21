package us.jcedeno.hangar.paper.tranciever.guis.creator.subgui;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
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
import us.jcedeno.hangar.paper.tranciever.guis.creator.CreatorGUI;

public class TeamSizeGUI extends RapidInv {
    private Integer size;

    public TeamSizeGUI(Player player, CreatorGUI creator, String title) {
        super(InventoryType.HOPPER, title);
        size = creator.getGameCreator().getTeam_size();

        setItem(2, getPlayerHead(player, getSizeName(size)), e -> {
            // Save and return
            var item = creator.getInventory().getItem(CreatorGUI.slot_for_teamsize);
            var meta = item.getItemMeta();
            meta.setDisplayName(getSizeName(size));
            item.setItemMeta(meta);
            creator.getGameCreator().setTeam_size(size);
            creator.open(player);
        });

        // Add substact items
        var less_item = new ItemBuilder(Material.TIPPED_ARROW).flags(ItemFlag.HIDE_POTION_EFFECTS)
                .name(ChatColor.WHITE + "Minus (-)").build();
        var meta = (PotionMeta) less_item.getItemMeta();
        meta.setColor(Color.fromRGB(240, 231, 74));
        less_item.setItemMeta(meta);

        setItem(0, less_item, e -> {
            if (size > 1) {
                size--;
                var item = getInventory().getItem(2);
                item.setAmount(size);
                updateDisplayName(item, getSizeName(size));
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.VOICE, 1.0f, 1.0f);
            }
        });

        var plus_item = new ItemBuilder(Material.TIPPED_ARROW).flags(ItemFlag.HIDE_POTION_EFFECTS)
                .name(ChatColor.WHITE + "Plus (+)").build();
        var plus_meta = (PotionMeta) plus_item.getItemMeta();
        plus_meta.setColor(Color.fromRGB(81, 219, 111));
        plus_item.setItemMeta(plus_meta);

        setItem(4, plus_item, e -> {
            if (size < 10) {
                size++;
                var item = getInventory().getItem(2);
                item.setAmount(size);
                updateDisplayName(item, getSizeName(size));
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.VOICE, 1.0f, 1.0f);
            }
        });
    }

    private String getSizeName(int size) {
        return ChatColor.YELLOW + "Team Size: " + ChatColor.WHITE + (size > 1 ? "To" + size : "FFA");
    }

    private ItemStack getPlayerHead(Player player, String name) {
        var player_head = new ItemStack(Material.PLAYER_HEAD, size);
        var skull = (SkullMeta) player_head.getItemMeta();
        skull.setOwningPlayer(player);
        skull.setDisplayName(name);
        skull.setLore(
                LoreBuilder.of(ChatColor.GRAY + "Right click to save.", ChatColor.GRAY + "Left click to return."));
        player_head.setItemMeta(skull);
        return player_head;
    }

    private void updateDisplayName(final ItemStack stack, String displayName) {
        var meta = stack.getItemMeta();
        meta.setDisplayName(displayName);
        stack.setItemMeta(meta);
    }

}
