package us.jcedeno.hangar.paper.uhc;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor(staticName = "of")
@Data
public class GameData {
    String gameID;
    Long startTime;
    int gameTime;
    boolean pvp;
    boolean privateGame;
    int uhcslots;
    String gameStage;
    int playersOnline;
    int playersAlive;
    int spectators;
    String[] scenarios;
    String gameType;
    String hostname;
    String ip;

}
