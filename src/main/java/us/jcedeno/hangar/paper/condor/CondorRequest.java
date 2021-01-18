package us.jcedeno.hangar.paper.condor;

import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class CondorRequest {
    String instanceType;
    String gameType;
    String[] scenarios;
    int teamSize;
    boolean privateGame;
    String whitelistID;
    UUID hostUUID;
    String host;

    /**
     * Transform the object into an stringified json.
     * 
     * @return Stringified Json.
     */
    public String toJson() {
        var json = new JsonObject();
        {
            var innerJson = new JsonObject();

            innerJson.addProperty("provider", "vultr");
            innerJson.addProperty("type", instanceType);

            json.add("instance_type", innerJson);
        }
        {
            var innerJson = new JsonObject();

            innerJson.addProperty("game_type", gameType);
            innerJson.add("scenarios", getScenariosAsArray());
            innerJson.addProperty("team_size", teamSize);
            innerJson.addProperty("private", privateGame);
            innerJson.addProperty("whitelist", whitelistID);

            json.add("config", innerJson);
        }
        json.addProperty("host", host);
        json.addProperty("host_uuid", hostUUID.toString());
        return json.toString();
    }

    JsonArray getScenariosAsArray() {
        var jsonArray = new JsonArray();
        for (var scenario : scenarios)
            jsonArray.add(scenario);

        return jsonArray;
    }
}
