package us.jcedeno.hangar.paper.communicator;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import fr.mrmicky.fastinv.FastInv;

/**
 * SelectorInventory
 */
public class SelectorInventory extends FastInv {

    public SelectorInventory(InventoryType type, String title) {
        super(type, title);

    }

    public ItemStack[] getItems(){
        return getInventory().getContents();
    }

    
}