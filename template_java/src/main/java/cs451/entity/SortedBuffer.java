package cs451.entity;

import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;

public class SortedBuffer {
    private final Comparator<Message> orderComparator = Comparator.comparingInt(Message::getSeq);
    private final ConcurrentSkipListSet<Message> sortedBuffer = new ConcurrentSkipListSet<>(orderComparator);
    private int watermark = 1;
    private int lowest = 2147483647;
    private boolean newLowest = true;

    public SortedBuffer() {
    }

    public ConcurrentSkipListSet<Message> getSortedBuffer() {
        return sortedBuffer;
    }

    public int getWatermark() {
        return watermark;
    }

    public int getLowest() {
        return lowest;
    }

    public boolean hasNewLowest() {
        return newLowest;
    }

    public void setLowest(int lowest) {
        this.lowest = lowest;
    }

    public void setNewLowest(boolean newLowest) {
        this.newLowest = newLowest;
    }

    public void incrementWatermark() {
        this.watermark ++;
    }
}
