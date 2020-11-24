package us.jcedeno.hangar.paper.tranciever.guis.creator.objects;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import fr.mrmicky.fastinv.ItemBuilder;
import net.md_5.bungee.api.ChatColor;

public enum ScenariosEnum {
    CUTCLEAN("Cutclean", Material.IRON_INGOT), TIMEBOMB("TimeBomb", Material.TNT),
    TIMBER("Timber", Material.OAK_LOG), NO_CLEAN("No Clean", Material.NAME_TAG),
    BACKPACK("Backpack", Material.ENDER_CHEST), NO_FALL("NoFall", Material.DIAMOND_BOOTS),
    FIRELESS("Fireless", Material.FIRE_CHARGE), GOTOHELL("Go to Hell", Material.WARPED_NYLIUM),
    BOWLESS("Bowless", Material.BOW), HASTEYBOYS("HasteyBoys", Material.GOLDEN_PICKAXE),
    NINESLOTS("NineSlots", Material.BARRIER), INFINITE_ENCHANTER("Infinite Enchanter", Material.BOOKSHELF),
    SWITCHEROO("Switcheroo", Material.ENDER_EYE), FLOWER_POWER("FlowerPower", Material.WITHER_ROSE),
    ADVANCEMENTHUNTER("Advancement Hunter", Material.CRAFTING_TABLE), FASTSMELTING("FastSmelting", Material.FURNACE), 
    BLOODENCHANTS("BloodEnchants", Material.ENCHANTING_TABLE), SKYHIGH("SkyHigh", Material.PHANTOM_MEMBRANE),
    GOLDENRETREIVER("GoldenRetreiver", Material.GOLDEN_APPLE), MONSTERSINC("MonstersInc", Material.CRIMSON_DOOR), 
    SECRET("UHC Vandálico", Material.EMERALD_BLOCK);
    
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
        var item = new ItemBuilder(material).name(ChatColor.of("#cd8bf8") + name).flags(ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ENCHANTS);
        var stack = item.build();

        if (enchanted)
            stack.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);

        return stack;
    }
}
