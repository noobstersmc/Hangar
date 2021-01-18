package us.jcedeno.hangar.paper.condor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewCondor {
    private @Getter static final Map<String, String> tokenMap = new HashMap<>();
    private @Getter static final Map<String, String> customInstanceType = new HashMap<>();
    private @Getter static final Map<String, String> customWhitelistId = new HashMap<>();
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    static OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS).build();
    public static String condorURI = "http://condor.jcedeno.us/";

    /**
     * Create a post request in condor.
     * 
     * @param auth Authorization token, also used as billing id.
     * @param json JSON with the specific request
     * @return Json Object in string form.
     * @throws IOException
     */
    public static String post(String auth, String json) throws IOException {
        var body = RequestBody.create(json, JSON);
        Request request = new Request.Builder().url(condorURI + "instances").addHeader("Authorization", auth)
                .addHeader("Content-Type", "application/json").post(body).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    /**
     * Creates a get request to condor.
     * 
     * @param auth Authorization token
     * @param url  Path and parameters using ?
     * @return Result depends on path but expect a json.
     * @throws IOException
     */
    public static String get(String auth, String url) throws IOException {
        Request request = new Request.Builder().url(condorURI + url).addHeader("Authorization", auth)
                .addHeader("Content-Type", "application/json").get().build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    /**
     * Array of all available compute in a vultr region.
     * 
     * @param region Vultr regions, by default use EWR (NY)
     * @return Collection of instance types. May be empty but not null.
     */
    public static ArrayList<String> getAvailability(String region) {
        var list = new ArrayList<String>();
        try {
            var response = client.newCall(new Request.Builder()
                    .url("https://api.vultr.com/v2/regions/" + region + "/availability").get().build()).execute();
            var json = gson.fromJson(response.body().string(), JsonObject.class);
            var instances = json.getAsJsonArray("available_plans");
            if (instances != null)
                instances.forEach(e -> list.add(e.getAsString()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }
}
