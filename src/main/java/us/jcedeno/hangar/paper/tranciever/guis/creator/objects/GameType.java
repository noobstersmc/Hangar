package us.jcedeno.hangar.paper.tranciever.guis.creator.objects;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import fr.mrmicky.fastinv.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.communicator.LoreBuilder;

public enum GameType {
    UHC("UHC", TerrainGeneration.VANILLA,
            new ItemBuilder(Material.NETHERITE_HELMET).name(ChatColor.YELLOW + "UHC Games")
                    .lore(ChatColor.WHITE + "Click to switch gamemodes.").flags(ItemFlag.HIDE_ATTRIBUTES).build(),
            new ItemStack(Material.GOLDEN_APPLE)),
    RUN("UHC Run", TerrainGeneration.RUN,
            new ItemBuilder(Material.DIAMOND_HELMET).name(ChatColor.YELLOW + "UHC Run Games")
                    .lore(ChatColor.WHITE + "Click to switch gamemodes.").flags(ItemFlag.HIDE_ATTRIBUTES).build(),
            new ItemBuilder(Material.APPLE).name(ChatColor.YELLOW + "UHC Run Match").build()),
    MEETUP("UHC Meetup", TerrainGeneration.VANILLA,
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

    private static ChatColor colorData = ChatColor.of("#82abba");
    private static ChatColor white = ChatColor.WHITE;
    private static ChatColor noobsters_red = ChatColor.of("#f49348");

    public ItemStack asServerDataIcon(UHCData data) {
        var item = serverDataIcon.clone();
        var meta = item.getItemMeta();
        var scenarioSize = data.getScenarios();
        final var stage = data.getGameStage();
        switch (stage.toLowerCase()) {
            case "ingame": {
                meta.setLore(LoreBuilder.of(colorData + "Config: " + white + data.getTeamSize() + " " + data.getScenarios(), "",
                colorData + "Game Time: " + white + timeConvert(data.getGameTime()),
                colorData + "Stage: " + white + data.getGameStage(),
                "",
                colorData + "Players Alive: " + white + data.getPlayersAlive(),
                colorData + "Spectators: " + white + data.getSpectators()));
                meta.setDisplayName(noobsters_red + (data.getHostname() != null ? data.getHostname() : this.toString()));
                item.setItemMeta(meta);

                break;
            }
            default: {
                meta.setLore(LoreBuilder.of(colorData + "Config: " + white + data.getTeamSize() + " " + data.getScenarios(), 
                "",
                colorData + "Players: " + white + data.getPlayersOnline() + "/" + data.getUhcslots(),
                colorData + "Stage: " + white + data.getGameStage()));
                meta.setDisplayName(noobsters_red + (data.getHostname() != null ? data.getHostname() : this.toString()));
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
        return name;
    }
}
