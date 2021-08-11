package us.jcedeno.hangar.paper.tranciever.guis.creator.subgui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import us.jcedeno.hangar.paper.communicator.LoreBuilder;
import us.jcedeno.hangar.paper.tranciever.guis.creator.CreatorGUI;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.GameCreator;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.ScenariosEnum;
import us.jcedeno.libs.rapidinv.ItemBuilder;
import us.jcedeno.libs.rapidinv.RapidInv;
import us.jcedeno.libs.rapidinv.utils.SlotPos;

public class ScenarioSelectorGUI extends RapidInv {
    private static ArrayList<Integer> defaultIntegers = getCenteredSlots();

    public ScenarioSelectorGUI(CreatorGUI creator, Player player) {
        super(6 * 9, "Scenarios Selector");
        // CENTER FRAME
        var iterator = defaultIntegers.iterator();
        for (var scen : ScenariosEnum.values()) {
            final int slot = iterator.next();
            setItem(slot, scen.asItem(creator.getGameCreator().getScenarios().contains(scen)), e -> {
                var item = getInventory().getItem(slot);

                Objects.requireNonNull(item, "item");

                if (item.getEnchantments().size() > 0) {
                    item.getEnchantments().keySet().forEach(item::removeEnchantment);
                    item.setLore(null);
                    creator.getGameCreator().getScenarios().remove(scen);
                } else {
                    item.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
                    item.setLore(LoreBuilder.of(ChatColor.GREEN + "Enabled"));
                    creator.getGameCreator().getScenarios().add(scen);
                }
                System.out.println(creator.getGameCreator().getScenarios().toString());

            });
        }
        setItem(SlotPos.from(4, 5), new ItemBuilder(Material.ACACIA_DOOR).name(ChatColor.YELLOW + "Go back")
                .legacyLore(LoreBuilder.of(ChatColor.WHITE + "Click to return.")).build(), e -> {
                    creator.getInventory().getItem(CreatorGUI.slot_for_scenarios)
                            .setLore(getScenariosLore(creator.getGameCreator()));
                    creator.open(player);
                });
    }

    private List<String> getScenariosLore(GameCreator creator) {
        var scenarios = creator.getScenarios();
        if (scenarios.isEmpty()) {
            return LoreBuilder.of(ChatColor.WHITE + " - Vanilla+");
        }
        var list = new ArrayList<String>();
        scenarios.forEach(scen -> {
            list.add(ChatColor.WHITE + " - " + scen.toString());
        });
        return list;
    }

    private static ArrayList<Integer> getCenteredSlots() {
        var matches = new ArrayList<Integer>();
        int range_1_start = SlotPos.from(1, 1);
        int range_1_end = SlotPos.from(7, 1);
        while (range_1_start <= range_1_end) {
            matches.add(range_1_start++);
        }
        int range_2_start = SlotPos.from(1, 2);
        int range_2_end = SlotPos.from(7, 2);
        while (range_2_start <= range_2_end) {
            matches.add(range_2_start++);
        }
        int range_3_start = SlotPos.from(1, 3);
        int range_3_end = SlotPos.from(7, 3);
        while (range_3_start <= range_3_end) {
            matches.add(range_3_start++);
        }
        return matches;
    }

}
