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
    HashSet<ScenariosEnum> scenarios = new HashSet<>();

    public String createJsonRequest(Player player) {
        if (!player.hasPermission("condor.create")) {
            return "denied";
        }
        var request_data = Map.of("host", player.getName(), "game_type", terrain.toString(), "instance_type",
                Map.of("provider", "vultr", "region", "us"), "extra_data",
                Map.of("level_seed", seed, "scenarios",
                        scenarios.stream().map(ScenariosEnum::toString).collect(Collectors.toList()), "team_size",
                        team_size));

        return gson.toJson(request_data);
    }
}
