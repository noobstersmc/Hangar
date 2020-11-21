package us.jcedeno.hangar.paper.tranciever.guis.creator.objects;

import java.util.HashSet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor(staticName = "ofAll")
@RequiredArgsConstructor(staticName = "of")
public class GameCreator {
    @NonNull
    GameType game_type;
    @NonNull
    TerrainGeneration terrain;
    String seed = "random";
    Integer team_size = 1;
    Boolean private_game = false;
    HashSet<ScenariosEnum> scenarios = new HashSet<>();
}
