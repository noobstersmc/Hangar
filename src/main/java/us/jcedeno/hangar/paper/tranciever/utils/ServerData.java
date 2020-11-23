package us.jcedeno.hangar.paper.tranciever.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;

import lombok.AllArgsConstructor;
import lombok.Data;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.GameType;

@AllArgsConstructor(staticName = "of")
@Data
public class ServerData {
    private static Random random = new Random();
    String ipv4;
    UUID game_id;
    boolean private_game = false;
    HashMap<String, Object> extra_data;
    //For dummy data creation
    static HashMap<String, Object> map = new HashMap<String, Object>();
    static{
        map.put("material", Material.ENCHANTED_GOLDEN_APPLE);
        map.put("game-type", GameType.UHC);
    }

    public static Set<ServerData> getDummyData(int amount, GameType type) {
        var set = new HashSet<ServerData>();
        map.put("game-type", type);
        map.put("material", type.getServerIcon());
        for (int i = 0; i < amount; i++)
            set.add(getDummyData(type));

        return set;
    }

    public static ServerData getDummyData(GameType type) {
        return of("localhost:" + getRandomPort(), UUID.randomUUID(), random.nextBoolean(), new HashMap<>(map));
    }

    private static int getRandomPort() {
        return random.nextInt(64_330) + 1023;
    }
}
