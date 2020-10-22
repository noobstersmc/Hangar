package us.jcedeno.hangar.paper.objects;

import org.bukkit.Location;
import org.bukkit.Material;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class BlockRestoreTask {
    //Location loc;
    String world;
    CoordinatePair coordinatePair;
    Material restoreTo;
    Material from;
    Long when;
    
}
