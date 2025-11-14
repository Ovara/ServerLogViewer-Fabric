package fish.crafting.logviewer.log.storage;

import fish.crafting.logviewer.log.LogLine;
import fish.crafting.logviewer.util.LogUtil;
import io.wispforest.owo.ui.core.Component;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LogChunk {

    @Getter
    private final LogLine[] data;
    private int occupied = 0;
    public boolean dirty = false;
    private long decaysAt = 0L;

    public LogChunk(int size){
        this.data = new LogLine[size];
    }

    /**
     * @return Overflow
     */
    public @Nullable LogLine[] insert(LogLine[] lines){
        if(lines.length <= freeSlots()){
            System.arraycopy(lines, 0, data, occupied, lines.length);
            occupied += lines.length;

            dirty = true;
            return null;
        }else{
            int left = freeSlots();
            int overflow = lines.length - left;
            System.arraycopy(lines, 0, data, occupied, left);
            occupied += left;

            LogLine[] out = new LogLine[overflow];
            System.arraycopy(lines, left, out, 0, overflow);

            dirty = true;
            return out;
        }
    }

    public List<Component> compile(){
        List<Component> comp = new ArrayList<>();

        for(int i = 0; i < occupied; i++){
            comp.add(data[i].component());
        }

        return comp;
    }

    public void decaysIn(int seconds){
        if(seconds == -1) {
            decaysAt = 0L;
        }else{
            decaysAt = System.currentTimeMillis() + seconds * 1000L;
        }
    }

    public void clampedDecaysIn(int seconds){
        if(seconds == -1){
            decaysAt = 0L;
            return;
        }

        if(decaysAt == 0L) {
            decaysIn(seconds);
            return;
        }

        long left = System.currentTimeMillis() - decaysAt;
        if(seconds * 1000L < left){
            decaysAt = System.currentTimeMillis() + seconds * 1000L;
        }
    }

    public boolean isDecayed(){
        return decaysAt != 0L && decaysAt < System.currentTimeMillis();
    }

    private int freeSlots(){
        return size() - occupied;
    }

    private int size(){
        return this.data.length;
    }

}
