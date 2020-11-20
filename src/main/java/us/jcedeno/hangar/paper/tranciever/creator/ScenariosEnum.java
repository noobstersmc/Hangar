package us.jcedeno.hangar.paper.tranciever.creator;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import fr.mrmicky.fastinv.ItemBuilder;
import net.md_5.bungee.api.ChatColor;

public enum ScenariosEnum {
    CUTCLEAN, TIMEBOMB, TIMBER, NO_CLEAN, BACKPACK, NO_FALL, FIRELESS, BLOOD_DIAMONDS, BOWLESS, HASTEYBOYS,
    GONE_FISHING, INFINITE_ENCHANTER, LIMITS, FLOWER_POWER, SWITCHEROO;

    @Override
    public String toString() {
        switch (this) {
            case BACKPACK:
                return "Backpack";
            case BLOOD_DIAMONDS:
                return "Blood Diamonds";
            case BOWLESS:
                return "Bowless";
            case CUTCLEAN:
                return "Cutclean";
            case FIRELESS:
                return "Fireless";
            case FLOWER_POWER:
                return "Flower Power";
            case GONE_FISHING:
                return "Gone Fishing";
            case HASTEYBOYS:
                return "Hastey Boys";
            case INFINITE_ENCHANTER:
                return "Infinite Enchanted";
            case LIMITS:
                return "Limits";
            case NO_CLEAN:
                return "No Clean";
            case NO_FALL:
                return "No Fall";
            case SWITCHEROO:
                return "Switcheroo";
            case TIMBER:
                return "Timber";
            case TIMEBOMB:
                return "Timebomb";
            default:
                return super.toString();

        }
    }

    public ItemStack asItem(boolean enchanted) {
        var item = new ItemBuilder(Material.IRON_INGOT);

        switch (this) {
            case CUTCLEAN:
                item = item.name(ChatColor.GOLD + "Cutclean");
                break;
            case NO_CLEAN:
                item = item.name(ChatColor.GOLD + "No Clean").type(Material.NAME_TAG);
                break;
            case TIMEBOMB:
                item = item.name(ChatColor.GOLD + "Timebomb").type(Material.TNT);
                break;
            case BACKPACK:
                item = item.name(ChatColor.GOLD + "Backpack").type(Material.CHEST);
                break;
            case FIRELESS:
                item = item.name(ChatColor.GOLD + "Fireless").type(Material.FLINT_AND_STEEL);
                break;
            case NO_FALL:
                item = item.name(ChatColor.GOLD + "No Fall").type(Material.DIAMOND_BOOTS);
                break;
            case TIMBER:
                item = item.name(ChatColor.GOLD + "Timber").type(Material.DIAMOND_AXE);
                break;
            case BLOOD_DIAMONDS:
                item = item.name(ChatColor.GOLD + "Blood Diamonds").type(Material.DIAMOND);
                break;
            case BOWLESS:
                item = item.name(ChatColor.GOLD + "Bowless").type(Material.BOW);
                break;
            case FLOWER_POWER:
                item = item.name(ChatColor.GOLD + "Flower Power").type(Material.WITHER_ROSE);
                break;
            case GONE_FISHING:
                item = item.name(ChatColor.GOLD + "Gone Fishing").type(Material.FISHING_ROD);
                break;
            case HASTEYBOYS:
                item = item.name(ChatColor.GOLD + "Hastey Boys").type(Material.GOLDEN_PICKAXE);
                break;
            case INFINITE_ENCHANTER:
                item = item.name(ChatColor.GOLD + "Infinite Enchanter").type(Material.BOOKSHELF);
                break;
            case LIMITS:
                item = item.name(ChatColor.GOLD + "Limits").type(Material.BARRIER);
                break;
            case SWITCHEROO:
                item = item.name(ChatColor.GOLD + "Switcheroo").type(Material.ENDER_EYE);
                break;
        }
        item = item.flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        var stack = item.build();
        if (enchanted)
            stack.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);

        return stack;
    }
}
