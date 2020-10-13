package us.jcedeno.hangar.paper.objects;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CoordinatePair {
    int radiusX, radiusY, radiusZ;

    public static CoordinatePair of(int x, int y, int z) {
        return builder().radiusX(x).radiusY(y).radiusZ(z).build();
    }
}
