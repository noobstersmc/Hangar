package us.jcedeno.hangar.paper.tranciever.guis.creator;

import java.io.IOException;
import java.util.stream.Collectors;

import com.destroystokyo.paper.Title;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import fr.mrmicky.fastinv.ItemBuilder;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.communicator.LoreBuilder;
import us.jcedeno.hangar.paper.condor.CondorRequest;
import us.jcedeno.hangar.paper.condor.NewCondor;
import us.jcedeno.hangar.paper.tranciever.RapidInv;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.GameCreator;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.GameType;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.TerrainGeneration;
import us.jcedeno.hangar.paper.tranciever.guis.creator.subgui.ScenarioSelectorGUI;
import us.jcedeno.hangar.paper.tranciever.guis.creator.subgui.TeamSizeGUI;
import us.jcedeno.hangar.paper.tranciever.guis.tranceiver.RecieverGUI;
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
    private Gson gson = new Gson();
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
            var token = NewCondor.getTokenMap().getOrDefault(clicker.getUniqueId().toString(),
                    clicker.getUniqueId().toString());

            var custom_instance = NewCondor.getCustomInstanceType().remove(clicker.getUniqueId().toString());
            var whitelist_id = NewCondor.getCustomWhitelistId().getOrDefault(clicker.getUniqueId().toString(), "null");

            var json_request_condor = CondorRequest.of(
                    custom_instance != null ? custom_instance : gameType.getDefaultInstance(), gameType.toString(),
                    gameCreator.getScenarios().stream().map(c -> c.toString()).collect(Collectors.toList())
                            .toArray(new String[] {}),
                    gameCreator.getTeam_size(), gameCreator.getPrivate_game(), whitelist_id, clicker.getUniqueId(),
                    clicker.getName());
            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                try {
                    var condor = NewCondor.post(token, json_request_condor.toJson());
                    System.out.println(condor);
                    var object = gson.fromJson(condor, JsonObject.class);
                    var error = object.get("error");
                    if (error != null) {
                        clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, SoundCategory.VOICE,
                                1.0f, 1.0f);
                        clicker.sendMessage(ChatColor.RED + error.getAsString());
                    } else {
                        var gid = object.get("game_id");
                        if (gid != null) {
                            clicker.sendMessage(
                                    ChatColor.GREEN + "Your request has been recived with ID " + gid.getAsString());
                            clicker.playSound(clicker.getLocation(), Sound.BLOCK_ANVIL_USE, SoundCategory.VOICE, 1.0f,
                                    1.0f);
                        } else {
                            clicker.sendMessage(ChatColor.RED + "Condor was not able to process request.");

                        }
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                    clicker.sendMessage(
                            ChatColor.RED + "Condor was not able to process request due to " + e1.getMessage());
                }

            });
            clicker.closeInventory();

        });
        setItem(slot_for_home, HOME_ITEM, e -> {
            var inv = getParentInventory();
            while (!(inv instanceof RecieverGUI)) {
                inv = inv.getParentInventory();
            }
            if (inv != null) {
                inv.open(e.getWhoClicked());
            }
        });
    }

    // Boilerplate
    private ItemStack VANILLA_GEN = new ItemBuilder(Material.GRASS_BLOCK).name(ChatColor.RED + "UHC Game").build();
    private ItemStack RUN_GEN = new ItemBuilder(Material.ANCIENT_DEBRIS).name(ChatColor.YELLOW + "UHC Run Game")
            .build();
    public ItemStack SEED_ITEM = new ItemBuilder(Material.WHEAT_SEEDS).name(ChatColor.YELLOW + "Seed:")
            .lore(ChatColor.WHITE + "random").build();
    public ItemStack TEAM_ITEM = new ItemBuilder(Material.DIAMOND_CHESTPLATE).flags(ItemFlag.HIDE_ATTRIBUTES)
            .name(ChatColor.YELLOW + "Team Size: " + ChatColor.WHITE + "FFA").build();
    public ItemStack SCENARIOS_ITEM = new ItemBuilder(Material.TOTEM_OF_UNDYING).name(ChatColor.YELLOW + "Scenarios")
            .lore(ChatColor.WHITE + " - Vanilla+").build();
    public ItemStack LAUNCH_ITEM = new ItemBuilder(Material.IRON_PICKAXE).flags(ItemFlag.HIDE_ATTRIBUTES)
            .name(ChatColor.of("#f49348") + "Launch Server").lore(LoreBuilder
                    .of(ChatColor.WHITE + "Click to launch the server", ChatColor.WHITE + "with the selected config."))
            .build();
    public ItemStack HOME_ITEM = new ItemBuilder(Material.WARPED_DOOR).name(ChatColor.of("#918bf8") + "Main menu")
            .build();

}
