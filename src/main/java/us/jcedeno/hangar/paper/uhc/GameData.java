package us.jcedeno.hangar.paper.uhc;

import lombok.Data;

@Data
public class GameData {
    String gameID;
    Long startTime;
    int gameTime;
    boolean pvp;
    int uhcSlots;
    String gameStage;
    int playersAlive;
    int spectators;
    String[] scenarios;
    
}
