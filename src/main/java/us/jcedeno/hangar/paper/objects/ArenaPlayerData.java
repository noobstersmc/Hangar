package us.jcedeno.hangar.paper.objects;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.PlayerInventory;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ArenaPlayerData {
    @NonNull
    UUID uuid;
    String[] playerInventory;
    String location;
    Long lastDamageTime;
    int currentKills = 0;

    public void serializeInventory(PlayerInventory inv) {
        this.playerInventory = InventorySerializer.playerInventoryToBase64(inv);
    }

    public void setSerializedLocation(Location loc) {
        this.location = loc.getWorld().getName() + ", " + loc.getBlockX() + ", " + loc.getBlockY() + ", "
                + loc.getBlockZ();
    }

    public Location getPosition() {
        if (location == null || location.isEmpty())
            return null;
        String split[] = location.split(", ");
        return new Location(Bukkit.getWorld(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]),
                Integer.parseInt(split[3]));
    }

}
