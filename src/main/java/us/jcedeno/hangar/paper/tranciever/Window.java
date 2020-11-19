package us.jcedeno.hangar.paper.tranciever;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class Window implements InventoryProvider {

    public static final SmartInventory INVENTORY = SmartInventory.builder()
            .id("myInventory")
            .provider(new Window())
            .size(3, 9)
            .title(ChatColor.BLUE + "My Awesome Inventory!")
            .build();

    private final Random random = new Random();

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(new ItemStack(Material.GRAY_STAINED_GLASS)));

        contents.set(1, 1, ClickableItem.of(new ItemStack(Material.CARROT),
                e -> player.sendMessage(ChatColor.GOLD + "You clicked on a potato.")));

        contents.set(1, 7, ClickableItem.of(new ItemStack(Material.BARRIER),
                e -> player.closeInventory()));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        int state = contents.property("state", 0);
        contents.setProperty("state", state + 1);

        if(state % 5 != 0)
            return;

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS, 1);
        contents.fillBorders(ClickableItem.empty(glass));
    }

}
