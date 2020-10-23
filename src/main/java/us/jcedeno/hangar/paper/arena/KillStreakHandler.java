package us.jcedeno.hangar.paper.arena;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import lombok.Getter;
import us.jcedeno.hangar.paper.Hangar;
import us.jcedeno.hangar.paper.events.KillStreakEvent;

public class KillStreakHandler implements Listener{
    private static String KILLSTREAK_JSON = Bukkit.getWorldContainer().getPath() + File.separatorChar
            + "arena-kill-streak.json";
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private @Getter ArrayList<KillStreak> streaks = new ArrayList<>();

    public KillStreakHandler(Hangar instance){
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onKillStreak(KillStreakEvent e) {
        try {
            var currentStreaks = getKillStreaks();
            var iterator = currentStreaks.iterator();
            boolean update = false;
            while (iterator.hasNext()) {
                var next = iterator.next();
                if (next.sameUser(e.getUuid())) {
                    if (e.getKills() > next.getKills()) {
                        update = true;
                        next.setKills(e.getKills());
                        next.setDate(e.getTimeOfDeath());
                        break;
                    }
                    return;
                }
            }
            if (!update) {
                currentStreaks.add(KillStreak.of(e.getUuid(), e.getTimeOfDeath(), e.getKills()));
            }
            Collections.sort(currentStreaks, Collections.reverseOrder());
            var trimmed = currentStreaks.subList(0, currentStreaks.size() >= 10 ? 10 : currentStreaks.size());
            saveKillStreaks(trimmed);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    

    public ArrayList<KillStreak> getKillStreaks() throws Exception {
        if (!streaks.isEmpty()) {
            return streaks;
        }
        var file = new File(KILLSTREAK_JSON);
        var ks = new ArrayList<KillStreak>();

        if (!file.exists()) {
            file.createNewFile();
            return ks;
        }
        var reader = Files.newBufferedReader(Paths.get(KILLSTREAK_JSON));
        var jsonArray = gson.fromJson(reader, JsonArray.class);

        jsonArray.forEach(element -> ks.add(gson.fromJson(element, KillStreak.class)));

        Collections.sort(ks, Collections.reverseOrder());

        reader.close();

        streaks = ks;

        return ks;
    }

    public void saveKillStreaks(List<KillStreak> arr) throws Exception {
        var writer = new FileWriter(KILLSTREAK_JSON);
        gson.toJson(arr, writer);
        writer.flush();
        writer.close();

        this.streaks = new ArrayList<>(arr);
    }
    
}
