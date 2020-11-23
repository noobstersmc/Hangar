package us.jcedeno.hangar.paper.tranciever.guis.creator.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import us.jcedeno.hangar.paper.uhc.GameData;

@AllArgsConstructor(staticName = "of")
@Data
public class UHCData {
    Long startTime;
    int gameTime;
    boolean pvp;
    boolean privateGame;
    int uhcslots;
    String gameStage;
    int playersOnline;
    int playersAlive;
    int spectators;
    String teamSize;
    String scenarios;
    String hostname;
    String displayName;

    public static UHCData fromOldFormat(GameData oldFormatData){
        return of(oldFormatData.getStartTime(), oldFormatData.getGameTime(), oldFormatData.isPvp(), oldFormatData.isPrivateGame(), oldFormatData.getPlayersOnline(), oldFormatData.getGameStage(), oldFormatData.getPlayersOnline(), oldFormatData.getPlayersAlive(), oldFormatData.getSpectators(), oldFormatData.getGameType(), oldFormatData.getScenarios(), oldFormatData.getHostname(), oldFormatData.getHostname());
    }
}
