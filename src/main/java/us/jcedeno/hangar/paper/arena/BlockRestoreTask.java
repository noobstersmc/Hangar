package us.jcedeno.hangar.paper.arena;

import org.bukkit.Material;

import lombok.AllArgsConstructor;
import lombok.Data;
import us.jcedeno.hangar.paper.objects.CoordinatePair;

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
