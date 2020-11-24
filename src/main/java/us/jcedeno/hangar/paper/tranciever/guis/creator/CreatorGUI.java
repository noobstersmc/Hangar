package us.jcedeno.hangar.paper.tranciever.guis.creator;

import com.destroystokyo.paper.Title;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import fr.mrmicky.fastinv.ItemBuilder;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.communicator.LoreBuilder;
import us.jcedeno.hangar.paper.tranciever.RapidInv;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.GameCreator;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.GameType;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.TerrainGeneration;
import us.jcedeno.hangar.paper.tranciever.guis.creator.subgui.ScenarioSelectorGUI;
import us.jcedeno.hangar.paper.tranciever.guis.creator.subgui.TeamSizeGUI;
import us.jcedeno.hangar.paper.tranciever.utils.GeneralizedInputTask;
import us.jcedeno.hangar.paper.tranciever.utils.SlotPos;

public class CreatorGUI extends RapidInv {

    private @Getter GameCreator gameCreator;
    // Boiler-plate starts
    public static int slot_for_gen = SlotPos.from(1, 1);
    public static int slot_for_seed = SlotPos.from(3, 1);
    public static int slot_for_teamsize = SlotPos.from(5, 1);
    public static int slot_for_scenarios = SlotPos.from(7, 1);
    public static int slot_for_launch = SlotPos.from(5, 3);
    public static int slot_for_home = SlotPos.from(3, 3);
    // Boiler-plate ends
    private TeamSizeGUI teamSizeGUI;
    private ScenarioSelectorGUI scenarioSelectorGUI;

    public CreatorGUI(String title, RapidInv parentInventory, Hangar instance, GameType gameType) {
        super(4 * 9, title);
        if (parentInventory != null) {
            setParentInventory(parentInventory);
        }

        this.gameCreator = gameType != null ? gameType.getDefaulGameCreator()
                : GameCreator.of(GameType.UHC, TerrainGeneration.VANILLA);

        setItem(slot_for_gen, gameCreator.getTerrain() == TerrainGeneration.VANILLA ? VANILLA_GEN : RUN_GEN, (e) -> {
            if (e.getCurrentItem().getType() == Material.GRASS_BLOCK) {
                getInventory().setItem(slot_for_gen, RUN_GEN);
                gameCreator.setTerrain(TerrainGeneration.RUN);
                gameCreator.setGame_type(GameType.RUN);
            } else {
                getInventory().setItem(slot_for_gen, VANILLA_GEN);
                gameCreator.setTerrain(TerrainGeneration.VANILLA);
                gameCreator.setGame_type(GameType.UHC);
            }
        });

        setItem(slot_for_seed, SEED_ITEM, e -> {
            var player = (Player) e.getWhoClicked();
            player.closeInventory();
            var screenMessage = Title.builder().stay(20).title(ChatColor.GREEN + "Please type")
                    .subtitle("your seed in chat");
            // Smoothly ask the user for input
            player.sendTitle(screenMessage.fadeIn(10).build());
            // Start the task
            GeneralizedInputTask.of(player, instance, (task) -> {
                player.sendTitle(screenMessage.fadeIn(0).build());
            }, userInput -> {
                open(player, instance);
                player.resetTitle();
                gameCreator.setSeed(userInput);
                getInventory().getItem(slot_for_seed).setLore(LoreBuilder.of(ChatColor.WHITE + userInput));
            }).start(20L, 19L);

        });
        setItem(slot_for_teamsize, TEAM_ITEM, e -> {
            var player = (Player) e.getWhoClicked();
            if (teamSizeGUI == null) {
                teamSizeGUI = new TeamSizeGUI(player, this, "Team Size");
            }
            teamSizeGUI.open(player);
        });

        setItem(slot_for_scenarios, SCENARIOS_ITEM, e -> {
            var player = (Player) e.getWhoClicked();
            if (scenarioSelectorGUI == null) {
                scenarioSelectorGUI = new ScenarioSelectorGUI(this, player);
            }
            scenarioSelectorGUI.open(player);
        });
        
        setItem(slot_for_launch, LAUNCH_ITEM, e -> {
            var clicker = (Player) e.getWhoClicked();
            var request = gameCreator.createJsonRequest(clicker);
            if (request.equalsIgnoreCase("denied")) {
                clicker.sendMessage(ChatColor.RED + "Condor is not available yet!");
            } else {
                clicker.sendMessage("Creating a server: " + request);
                Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                    var condor = instance.getCondorManager();
                    try {
                        System.out.println(request.toString());
                        var result = condor.post(condor.create_game_url, request);
                        var condor_id = new Gson().fromJson(result, JsonObject.class).get("condor_id");
                        System.out.println(condor_id);
                        instance.getCommunicatorManager().getJedis().set("servers:data:" + condor_id, request);
                    } catch (Exception e1) {
                        clicker.sendMessage(ChatColor.RED + e1.getMessage() + ". Please report this to an admin!");
                        e1.printStackTrace();
                    }

                });
            }
            clicker.closeInventory();

        });
        setItem(slot_for_home, HOME_ITEM, e -> {
            getParentInventory().open((Player) e.getWhoClicked());
        });
    }

    // Boilerplate
    private ItemStack VANILLA_GEN = new ItemBuilder(Material.GRASS_BLOCK).name(ChatColor.YELLOW + "Vanilla Generation")
            .build();
    private ItemStack RUN_GEN = new ItemBuilder(Material.ANCIENT_DEBRIS).name(ChatColor.YELLOW + "Run Generation")
            .build();
    public ItemStack SEED_ITEM = new ItemBuilder(Material.WHEAT_SEEDS).name(ChatColor.YELLOW + "Seed:")
            .lore(ChatColor.WHITE + "random").build();
    public ItemStack TEAM_ITEM = new ItemBuilder(Material.DIAMOND_CHESTPLATE).flags(ItemFlag.HIDE_ATTRIBUTES)
            .name(ChatColor.YELLOW + "Team Size: " + ChatColor.WHITE + "FFA").build();
    public ItemStack SCENARIOS_ITEM = new ItemBuilder(Material.TOTEM_OF_UNDYING).name(ChatColor.YELLOW + "Scenarios")
            .lore(ChatColor.WHITE + " - Vanilla+").build();
    public ItemStack LAUNCH_ITEM = new ItemBuilder(Material.IRON_PICKAXE).name(ChatColor.GOLD + "Create!")
            .flags(ItemFlag.HIDE_ATTRIBUTES).build();
    public ItemStack HOME_ITEM = new ItemBuilder(Material.ACACIA_DOOR).name(ChatColor.WHITE + "Main menu").build();

}
