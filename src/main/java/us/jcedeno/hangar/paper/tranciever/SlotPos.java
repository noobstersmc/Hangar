package us.jcedeno.hangar.paper.tranciever;

public class SlotPos {
    public static int from(int row, int column) {
        return row + (column * 9);
    }

}
