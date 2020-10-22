package us.jcedeno.hangar.paper.objects;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KillStreak implements Comparable<KillStreak> {
    UUID player;
    long date;
    int kills;

    @Override
    public int compareTo(KillStreak to) {
        if (this.kills == to.kills) {
            return (int) (to.date - this.date);
        }
        return this.kills - to.kills;
    }


    public static KillStreak of(UUID uuid, long time, int kill) {
        return new KillStreak(uuid, time, kill);
    }

    public boolean sameUser(UUID id) {
        return this.player.compareTo(id) == 0;
    }

    public boolean greaterThan(KillStreak other) {
        return other.kills > this.kills;
    }
}
