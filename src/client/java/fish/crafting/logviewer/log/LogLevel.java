package fish.crafting.logviewer.log;

import lombok.Getter;
import net.minecraft.util.Formatting;

import java.awt.*;

public enum LogLevel {
    SYSTEM(0x91A0B7, "System"),
    INFO(0x9FB5A6, "Info"),
    WARN(0xFFDC30, "Warn"),
    ERROR(0xFF303A, "Error")
    ;

    @Getter
    private final int color;
    @Getter
    private final String properName;

    LogLevel(int color, String properName){
        this.color = color;
        this.properName = properName;
    }

}
