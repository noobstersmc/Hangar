package us.jcedeno.hangar.paper.commands;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.bukkit.command.CommandSender;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.condor.NewCondor;

@CommandAlias("twitter")
public @RequiredArgsConstructor class TweetCMD extends BaseCommand {
    private @NonNull Hangar instance;
    private static Gson gson = new Gson();

    @Default
    @CommandPermission("tweet.cmd")
    @Subcommand("tweet")
    @CommandAlias("tweet")
    public void tweet(CommandSender sender, String tweet) throws IOException {
        var response = NewCondor.tweet(tweet, "6QR3W05K3F");

        var url = gson.fromJson(response, JsonObject.class).get("url");
        if (url != null) {
            var _url = url.getAsString();
            sender.sendMessage(ChatColor.GREEN + "Tweet send! \n" + _url);
        } else {
            sender.sendMessage(ChatColor.RED + "Couldn't send tweet!");
        }
    }

    @CommandPermission("post.cmd")
    @Subcommand("post")
    @CommandAlias("post")
    public void post(CommandSender sender, Integer time, String... gameConfig) {
        try {

            var game = "";
            for (var option : gameConfig) {
                game = game + option + " ";
            }
            sender.sendMessage(game);

            var future = getTimeInFuture(time);
            var formatted = DateTimeFormatter.ofPattern("hh:mm").format(future);
            var timeLeft = getTimeLeft(future);
            if (time < 10 || time > 30) {
                sender.sendMessage(ChatColor.RED
                        + "Couldn't post must be more than 10 minutes in advance and less than 30 minutes in advance.");
            } else {
                String tweet =

                        "UHC 1.16.X" + "\n\n" + game + "\n" + "1h + Meetup \n\n" + timeLeft + "\n" + formatted
                                + " (https://time.is/ET) \n\n IP noobsters.net";

                tweet(sender, tweet);
            }

        } catch (Exception e) {

        }
    }

    static LocalDateTime getTimeInFuture(long m) {
        var time = LocalDateTime.now(ZoneId.of("America/New_York")).plusMinutes(m).withSecond(0);

        var min = time.getMinute();
        var module = (int) min % 5;
        if (module != 0) {
            min += (5 - module);
        }
        time = time.plusMinutes(Math.abs(min - time.getMinute()));

        return time;

    }

    static String getTimeLeft(LocalDateTime time) throws ParseException {

        final var apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        apiFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        final Date dateOfGame = apiFormat.parse(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").format(time));
        final long millis = dateOfGame.getTime() - System.currentTimeMillis() - 1000;
        var hours = TimeUnit.MILLISECONDS.toHours(millis);
        var mins = TimeUnit.MILLISECONDS.toMinutes(millis)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));

        return "In " + (hours != 0 ? hours + "h " : "") + (mins != 0 ? mins + "m" : "");
    }

}