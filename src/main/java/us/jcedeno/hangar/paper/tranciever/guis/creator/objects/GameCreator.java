package us.jcedeno.hangar.paper.tranciever.guis.creator.objects;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.bukkit.entity.Player;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor(staticName = "ofAll")
@RequiredArgsConstructor(staticName = "of")
public class GameCreator {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @NonNull
    GameType game_type;
    @NonNull
    TerrainGeneration terrain;
    String seed = "random";
    Integer team_size = 1;
    Boolean private_game = false;
    Token token;
    HashSet<ScenariosEnum> scenarios = new HashSet<>();

    public String createJsonRequest(Player player) {
        if (!player.hasPermission("condor.create")) {
            return "denied";
        }
        var request_data = Map.of("host", player.getName(), "host_uuid", player.getUniqueId(), "limit",
                getLimit(player), "game_type", terrain.toString(), "instance_type",
                Map.of("provider", "vultr", "region", "us"), "extra_data",
                Map.of("level_seed", seed, "scenarios",
                        scenarios.stream().map(ScenariosEnum::toString).collect(Collectors.toList()), "team_size",
                        team_size));

        return gson.toJson(request_data);
    }
    public void initToken(Player player){        
        token = new Token(player);
    }

    public void togglePrivate() {
        private_game = !private_game;
    }

    public static int getLimit(Player player) {
        // Unlimited servers -1
        if (player.hasPermission("condor.limit.*"))
            return -1;
        // Check for permission node of 0 >= x > 10
        for (int i = 0; i < 10; i++)
            if (player.hasPermission("condor.limit." + i))
                return i;
        // Return 0 if not allowed
        return 0;
    }
}