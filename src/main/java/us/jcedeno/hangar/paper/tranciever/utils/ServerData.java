package us.jcedeno.hangar.paper.tranciever.utils;

import java.util.HashMap;
import java.util.UUID;

import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.Data;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.GameType;
import us.jcedeno.hangar.paper.tranciever.guis.creator.objects.UHCData;

@AllArgsConstructor(staticName = "of")
@Data
public class ServerData {
    private static Gson gson = new Gson();
    String ipv4;
    UUID game_id;
    boolean private_game = false;
    HashMap<String, Object> extra_data;

    public GameType getGameType() {
        return GameType.valueOf(extra_data.get("game-type").toString());
    }
    public UHCData getUhcData() {
        return gson.fromJson(extra_data.get("uhc-data").toString(), UHCData.class);
    }
}
