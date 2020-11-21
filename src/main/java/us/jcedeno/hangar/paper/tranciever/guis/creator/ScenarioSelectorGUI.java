package us.jcedeno.hangar.paper.tranciever.guis.creator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import fr.mrmicky.fastinv.ItemBuilder;
import us.jcedeno.hangar.paper.communicator.LoreBuilder;
import us.jcedeno.hangar.paper.tranciever.RapidInv;
import us.jcedeno.hangar.paper.tranciever.utils.ScenariosEnum;
import us.jcedeno.hangar.paper.tranciever.utils.SlotPos;

public class ScenarioSelectorGUI extends RapidInv {
    private CreatorGUI creator;
    private Player player;

    public ScenarioSelectorGUI(CreatorGUI creator, Player player) {
        super(6 * 9, "Scenarios Selector");
        this.player = player;
        this.creator = creator;
        // CENTER FRAME
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
        var iterator = matches.iterator();
        for (var scen : ScenariosEnum.values()) {
            final int slot = iterator.next();
            setItem(slot, scen.asItem(creator.getCreatorObject().getScenarios().contains(scen)), e -> {
                var item = getInventory().getItem(slot);

                Objects.requireNonNull(item, "item");

                if (item.getEnchantments().size() > 0) {
                    item.getEnchantments().keySet().forEach(item::removeEnchantment);
                    item.setLore(null);
                    creator.getCreatorObject().getScenarios().remove(scen);
                } else {
                    item.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
                    item.setLore(LoreBuilder.of(ChatColor.GREEN + "Enabled"));
                    creator.getCreatorObject().getScenarios().add(scen);
                }
                System.out.println(creator.getCreatorObject().getScenarios().toString());

            });
        }
        setItem(SlotPos.from(3, 5),
                new ItemBuilder(Material.ACACIA_DOOR).name(ChatColor.YELLOW + "Go back")
                        .lore(LoreBuilder.of(ChatColor.WHITE + "Right click to save and return",
                                ChatColor.WHITE + "Left click to return"))
                        .build(),
                e -> {
                    creator.getInventory().getItem(creator.slot_for_scenarios).setLore(getScenariosLore());
                    creator.open(player);
                });
        setItem(SlotPos.from(5, 5),
                new ItemBuilder(Material.IRON_PICKAXE).flags(ItemFlag.HIDE_ATTRIBUTES)
                        .name(ChatColor.YELLOW + "Launch Server")
                        .lore(LoreBuilder.of(ChatColor.WHITE + "Click to launch the server",
                                ChatColor.WHITE + "with the selected config."))
                        .build());

    }

    private List<String> getScenariosLore() {
        var scenarios = creator.getCreatorObject().getScenarios();
        if (scenarios.isEmpty()) {
            return LoreBuilder.of(ChatColor.WHITE + " - Vanilla+");
        }
        var list = new ArrayList<String>();
        scenarios.forEach(scen -> {
            list.add(ChatColor.WHITE + " - " + scen.toString());
        });
        return list;
    }

}
