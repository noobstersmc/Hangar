package us.jcedeno.hangar.paper.condor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import us.jcedeno.hangar.paper.Hangar;

@RequiredArgsConstructor
public class CondorManager {
    private Hangar instance;
    private CondorCommand condorCommand;

    public CondorManager(Hangar instance){
        this.instance = instance;
        this.condorCommand = new CondorCommand(this.instance);
    }

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    static OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(15, TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS).build();
    public String create_game_url = "http://condor.jcedeno.us:420/create-server";

    public String createMatch(String host, String gameType, String provider, String region, String seed)
            throws Exception {
        var json = createGame(host, gameType, provider, region, seed);

        var createServer = post(create_game_url, json);
        return createServer;
    }

    public String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder().url(url).addHeader("auth", "Condor-Secreto")
                .addHeader("Content-Type", "application/json").post(body).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    private String createGame(String host, String gameType, String provider, String region, String seed) {
        var condorGame = CondorGame.of(host, "displayname", gameType, Map.of("provider", provider, "region", region),
                Map.of("level_seed", seed));
        return gson.toJson(condorGame);
    }

}
