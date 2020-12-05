package us.jcedeno.hangar.paper.communicator;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

/**
 * SelectorInventory
 */
public class SelectorInventory extends RapidInv {

    public SelectorInventory(InventoryType type, String title) {
        super(type, title);

    }

    public ItemStack[] getItems(){
        return getInventory().getContents();
    }

    
}