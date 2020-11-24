package us.jcedeno.hangar.paper.tranciever.guis.creator.objects;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import fr.mrmicky.fastinv.ItemBuilder;
import net.md_5.bungee.api.ChatColor;

public enum ScenariosEnum {
    CUTCLEAN("Cutclean", Material.IRON_INGOT), TIMEBOMB("Timebomb", Material.TNT),
    TIMBER("Timber", Material.DIAMOND_AXE), NO_CLEAN("No Clean", Material.NAME_TAG),
    BACKPACK("Backpack", Material.ENDER_CHEST), NO_FALL("No Fall", Material.DIAMOND_BOOTS),
    FIRELESS("Fireless", Material.FIRE_CHARGE), BLOOD_DIAMONDS("Blood Diamonds", Material.DIAMOND),
    BOWLESS("Bowless", Material.BOW), HASTEYBOYS("Hasteyboys", Material.GOLDEN_PICKAXE),
    GONE_FISHING("Gone Fishing", Material.FISHING_ROD), INFINITE_ENCHANTER("Infinite Enchanter", Material.BOOKSHELF),
    LIMITS("Limits", Material.BARRIER), FLOWER_POWER("Flower Power", Material.WITHER_ROSE),
    SWITCHEROO("Switcheroo", Material.ENDER_EYE);

    String name;
    Material material;

    ScenariosEnum(String name, Material material) {
        this.name = name;
        this.material = material;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public ItemStack asItem(boolean enchanted) {
        var item = new ItemBuilder(material).name(ChatColor.GOLD + name).flags(ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ENCHANTS);
        var stack = item.build();

        if (enchanted)
            stack.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);

        return stack;
    }
}
