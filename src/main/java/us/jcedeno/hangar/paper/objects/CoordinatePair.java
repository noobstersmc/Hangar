package us.jcedeno.hangar.paper.objects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CoordinatePair {
    int radiusX, radiusY, radiusZ;

    public static CoordinatePair of(int x, int y, int z) {
        return builder().radiusX(x).radiusY(y).radiusZ(z).build();
    }

    public boolean equalToBlock(Block block) {
        return block.getX() == radiusX && block.getY() == radiusY && block.getZ() == radiusZ;
    }

    public Block toBlock(World world){
        return world.getBlockAt(new Location(world, radiusX, radiusY, radiusZ));
    }
}
