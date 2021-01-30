package us.jcedeno.hangar.paper.tranciever.guis.creator.objects;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import fr.mrmicky.fastinv.ItemBuilder;
import net.md_5.bungee.api.ChatColor;

public enum GameType {
    UHC("uhc", TerrainGeneration.VANILLA,
            new ItemBuilder(Material.NETHERITE_HELMET).name(ChatColor.YELLOW + "UHC Games")
                    .lore(ChatColor.WHITE + "Click to switch gamemodes.").flags(ItemFlag.HIDE_ATTRIBUTES).build(),
            new ItemStack(Material.GOLDEN_APPLE)),
    RUN("uhc-run", TerrainGeneration.RUN,
            new ItemBuilder(Material.DIAMOND_HELMET).name(ChatColor.YELLOW + "UHC Run Games")
                    .lore(ChatColor.WHITE + "Click to switch gamemodes.").flags(ItemFlag.HIDE_ATTRIBUTES).build(),
            new ItemBuilder(Material.APPLE).name(ChatColor.YELLOW + "UHC Run Match").build()),
    MEETUP("uhc-meetup", TerrainGeneration.VANILLA,
            new ItemBuilder(Material.IRON_HELMET).name(ChatColor.YELLOW + "UHC Meetup Games")
                    .flags(ItemFlag.HIDE_ATTRIBUTES).lore(ChatColor.WHITE + "Click to switch gamemodes.").build(),
            new ItemBuilder(Material.EMERALD).name(ChatColor.YELLOW + "UHC Meetup Match")
                    .flags(ItemFlag.HIDE_ATTRIBUTES).build());

    String name;
    TerrainGeneration defaulTerrainGeneration;
    ItemStack itemStack, serverDataIcon;
    Material material;
    static ItemStack noGames = new ItemBuilder(Material.BARRIER).name(ChatColor.RED + "No Games Found").build();

    GameType(String name, TerrainGeneration defaulTerrainGeneration, ItemStack itemStack, ItemStack serverDataIcon) {
        this.name = name;
        this.defaulTerrainGeneration = defaulTerrainGeneration;
        this.itemStack = itemStack;
        this.serverDataIcon = serverDataIcon;
    }

    public Material getServerIcon() {
        return material;
    }

    public String getDefaultInstance() {
        switch (this) {
            case UHC:
                return "vhf-3c-8gb";
            case RUN:
                return "vhf-2c-4gb";
            case MEETUP:
                return "vhf-1c-2gb";
        }
        return "";
    }

    private static ChatColor colorData = ChatColor.of("#82abba");
    private static ChatColor white = ChatColor.WHITE;
    private static ChatColor noobsters_red = ChatColor.of("#f49348");

    public ItemStack asServerDataIcon(UHCData data) {
        var item = serverDataIcon.clone();
        var meta = item.getItemMeta();
        final var stage = data.getGameStage();
        switch (stage.toLowerCase()) {
            case "ingame": {
                var lore = new ArrayList<String>();
                lore.add(colorData + "Game Time: " + white + timeConvert(data.getGameTime()));
                lore.add(colorData + "Stage: " + white + data.getGameStage());
                lore.add(" ");

                lore.add(colorData + "Config: " + white + data.getTeamSize());
                if (data.getScenarios().length > 0) {
                    for (var scenario : data.getScenarios()) {
                        lore.add(ChatColor.WHITE + " - " + scenario);
                    }
                } else {
                    lore.add(ChatColor.WHITE + " - Vanilla+");
                }

                lore.add(" ");
                lore.add(colorData + "Players Alive: " + white + data.getPlayersAlive());
                lore.add(colorData + "Spectators: " + white + data.getSpectators());
                meta.setLore(lore);
                meta.setDisplayName(
                        noobsters_red + (data.getHostname() != null ? data.getHostname() : this.toString()));
                item.setItemMeta(meta);

                break;
            }
            default: {
                var lore = new ArrayList<String>();
                lore.add(colorData + "Config: " + white + data.getTeamSize());
                if (data.getScenarios().length > 0) {
                    for (var scenario : data.getScenarios()) {
                        lore.add(ChatColor.WHITE + " - " + scenario);
                    }
                } else {
                    lore.add(ChatColor.WHITE + " - Vanilla+");
                }
                lore.add(colorData + "Players: " + white + data.getPlayersOnline() + "/" + data.getUhcslots());
                lore.add(colorData + "Stage: " + white + data.getGameStage());
                meta.setLore(lore);
                meta.setDisplayName(
                        noobsters_red + (data.getHostname() != null ? data.getHostname() : this.toString()));
                item.setItemMeta(meta);

                break;
            }
        }

        return item;
    }

    private String timeConvert(int t) {
        int hours = t / 3600;

        int minutes = (t % 3600) / 60;
        int seconds = t % 60;

        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds)
                : String.format("%02d:%02d", minutes, seconds);
    }

    public TerrainGeneration getDefaulTerrainGeneration() {
        return defaulTerrainGeneration;
    }

    public ItemStack getBrowserIcon() {
        return itemStack;
    }

    public ItemStack getDefaultItem() {
        return noGames;
    }

    public GameType getNextType() {
        return values()[(this.ordinal() >= values().length - 1 ? 0 : this.ordinal() + 1)];
    }

    public GameType getPreviousType() {
        // TODO:MAKE IT ACTUALLY GO BACK
        return getNextType();
    }

    public GameCreator getDefaulGameCreator() {
        return GameCreator.of(this, this.getDefaulTerrainGeneration());
    }

    @Override
    public String toString() {
        return name.toUpperCase();
    }
}
