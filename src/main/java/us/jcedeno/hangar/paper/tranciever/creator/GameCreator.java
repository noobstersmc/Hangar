package us.jcedeno.hangar.paper.tranciever.creator;

import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GameCreator {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @Builder.Default
    GameType game_type = GameType.UHC;
    @Builder.Default
    TerrainGeneration terrain = TerrainGeneration.VANILLA;
    @Builder.Default
    String seed = "random";
    @Builder.Default
    Integer team_size = 1;
    @Builder.Default
    Boolean private_game = false;
    @Builder.Default
    List<String> scenarios = Collections.emptyList();

    public String toString() {
        return gson.toJson(this);
    }
}
