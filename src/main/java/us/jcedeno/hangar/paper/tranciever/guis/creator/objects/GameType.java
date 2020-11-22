package us.jcedeno.hangar.paper.tranciever.guis.creator.objects;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import fr.mrmicky.fastinv.ItemBuilder;
import net.md_5.bungee.api.ChatColor;

public enum GameType {
    UHC("UHC", TerrainGeneration.VANILLA,
            new ItemBuilder(Material.NETHERITE_HELMET).name(ChatColor.YELLOW + "UHC Games")
                    .lore(ChatColor.WHITE + "Click to change gamemodes.").build()),
    RUN("UHC-RUN", TerrainGeneration.RUN,
            new ItemBuilder(Material.DIAMOND_HELMET).name(ChatColor.YELLOW + "UHC Run Games")
                    .lore(ChatColor.WHITE + "Click to change gamemodes.").build()),
    MEETUP("UHC-Meetup", TerrainGeneration.VANILLA, new ItemBuilder(Material.IRON_HELMET)
            .name(ChatColor.YELLOW + "UHC Meetup Games").lore(ChatColor.WHITE + "Click to change gamemodes.").build());

    String name;
    TerrainGeneration defaulTerrainGeneration;
    ItemStack itemStack;

    GameType(String name, TerrainGeneration defaulTerrainGeneration, ItemStack itemStack) {
        this.name = name;
        this.defaulTerrainGeneration = defaulTerrainGeneration;
        this.itemStack = itemStack;

    }

    public TerrainGeneration getDefaulTerrainGeneration() {
        return defaulTerrainGeneration;
    }

    public ItemStack getBrowserIcon() {
        return itemStack;
    }
    public ItemStack getDefaultItem() {
        return itemStack;
    }

    public GameType getNextType() {
        final var current = this.ordinal();
        if (current >= values().length) {
            return values()[0];
        }
        final var next = current + 1;
        return values()[next];
    }

    public GameCreator getDefaulGameCreator() {
        return GameCreator.of(this, this.getDefaulTerrainGeneration());
    }

    @Override
    public String toString() {
        return name;
    }
}
