package us.jcedeno.hangar.paper.tranciever.utils;

public class SlotPos {
    /**
     * 
     * @param row    From 0 - 8.
     * @param column From 0 - 5. multiples by 9.
     * @return Index position for specific slot.
     */
    public static int from(int row, int column) {
        return row + (column * 9);
    }

}
