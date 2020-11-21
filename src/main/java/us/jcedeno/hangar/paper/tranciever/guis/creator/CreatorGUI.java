package us.jcedeno.hangar.paper.tranciever.guis.creator;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import fr.mrmicky.fastinv.ItemBuilder;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.tranciever.RapidInv;
import us.jcedeno.hangar.paper.tranciever.utils.InputTask;
import us.jcedeno.hangar.paper.tranciever.utils.SlotPos;
import us.jcedeno.hangar.paper.tranciever.utils.TerrainGeneration;

public class CreatorGUI extends RapidInv {
    private RapidInv parentGui;
    private @Getter GameCreator creatorObject = GameCreator.builder().build();

    public CreatorGUI(String title, Hangar instance, RapidInv parentGui) {
        super(4 * 9, title);
        this.parentGui = parentGui;
        setItem(slot_for_gen, VANILLA_GEN, (e) -> {
            if (e.getCurrentItem().getType() == Material.GRASS_BLOCK) {
                getInventory().setItem(slot_for_gen, RUN_GEN);
                creatorObject.setTerrain(TerrainGeneration.RUN);
                creatorObject.setGame_type(GameType.RUN);
            } else {
                getInventory().setItem(slot_for_gen, VANILLA_GEN);
                creatorObject.setTerrain(TerrainGeneration.VANILLA);
                creatorObject.setGame_type(GameType.UHC);
            }
        });

        setItem(slot_for_seed, SEED_ITEM, e -> {
            var player = (Player) e.getWhoClicked();
            player.closeInventory();
            var task = new InputTask(player, this, instance);
            task.runTaskTimerAsynchronously(instance, 0L, 20L);
        });
        setItem(slot_for_teamsize, TEAM_ITEM, e -> {
            var player = (Player) e.getWhoClicked();
            var teamSizeSelector = new TeamSizeGUI(player, this, "Team Size");
            teamSizeSelector.open(player);
        });

        setItem(slot_for_scenarios, SCENARIOS_ITEM, e -> {
            var player = (Player) e.getWhoClicked();
            var scenarioSelector = new ScenarioSelectorGUI(this, player);
            scenarioSelector.open(player);
        });
        setItem(slot_for_launch, LAUNCH_ITEM, e -> {
            System.out.println(creatorObject.toString());
        });
        setItem(slot_for_home, HOME_ITEM, e -> {
            parentGui.open((Player) e.getWhoClicked());
        });
    }

    private ItemStack VANILLA_GEN = new ItemBuilder(Material.GRASS_BLOCK).name(ChatColor.YELLOW + "Vanilla Generation")
            .build();
    private ItemStack RUN_GEN = new ItemBuilder(Material.ANCIENT_DEBRIS).name(ChatColor.YELLOW + "Run Generation")
            .build();
    public ItemStack SEED_ITEM = new ItemBuilder(Material.WHEAT_SEEDS).name(ChatColor.YELLOW + "Seed:")
            .lore(ChatColor.WHITE + "random").build();
    public ItemStack TEAM_ITEM = new ItemBuilder(Material.DIAMOND_CHESTPLATE).flags(ItemFlag.HIDE_ATTRIBUTES)
            .name(ChatColor.YELLOW + "Team Size").lore(ChatColor.WHITE + "FFA").build();
    public ItemStack SCENARIOS_ITEM = new ItemBuilder(Material.TOTEM_OF_UNDYING).name(ChatColor.YELLOW + "Scenarios")
            .lore(ChatColor.WHITE + " - Vanilla+").build();
    public ItemStack LAUNCH_ITEM = new ItemBuilder(Material.IRON_PICKAXE).name(ChatColor.GOLD + "Create!")
            .flags(ItemFlag.HIDE_ATTRIBUTES).build();
    public ItemStack HOME_ITEM = new ItemBuilder(Material.ACACIA_DOOR).name(ChatColor.WHITE + "Main menu").build();
    public int slot_for_gen = SlotPos.from(1, 1);
    public int slot_for_seed = SlotPos.from(3, 1);
    public int slot_for_teamsize = SlotPos.from(5, 1);
    public int slot_for_scenarios = SlotPos.from(7, 1);
    public int slot_for_launch = SlotPos.from(5, 3);
    public int slot_for_home = SlotPos.from(3, 3);
}
