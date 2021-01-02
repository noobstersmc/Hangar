package us.jcedeno.hangar.paper.twitter;

import com.google.gson.Gson;

import kong.unirest.Unirest;

/**
 * LairTwitter
 */
public class LairTwitter {
    private static Gson gson = new Gson();
    private static String CONDOR_URL = "http://condor.jcedeno.us:420";

    static{
        Unirest.config().connectTimeout(1000);
    }

    public static String tweet(String tweet) {
        // Create the tweet as json
        var tweet_json = LairTweet.of(tweet).toJson(gson);
        // Create the request and call for response
        var response = Unirest.post(CONDOR_URL + "/tweet").header("auth", "Condor-Secreto")
                .header("Content-Type", "application/json").body(tweet_json).asString();
        // Return response's body
        return response.getBody();
    }
}