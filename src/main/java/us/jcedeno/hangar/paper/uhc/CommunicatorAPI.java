package us.jcedeno.hangar.paper.uhc;

import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.bukkit.Bukkit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CommunicatorAPI {
    
    private final static OkHttpClient client = new OkHttpClient();
    private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    
    public static CompletableFuture<GameData> getGameData(String URL) {
        return CompletableFuture.supplyAsync(() -> {
            var request = new Request.Builder().url(URL + "/uhc/config").build();
            try(Response response = client.newCall(request).execute()){ 
                if (response.isSuccessful()){
                    return gson.fromJson(response.body().string(), GameData.class);
                }

            }catch(Exception e){
                Bukkit.getLogger().warning("Data couldn't be retreived from " + URL + ". Error: " + e.getCause().getLocalizedMessage());
            }
            return null;
        });

    }
}
