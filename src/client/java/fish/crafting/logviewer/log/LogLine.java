package fish.crafting.logviewer.log;

import fish.crafting.logviewer.settings.custom.UISettings;
import fish.crafting.logviewer.ui.component.HorizontalFlowLayout;
import fish.crafting.logviewer.ui.component.LogLineComponent;
import fish.crafting.logviewer.ui.component.LogSideBarComponent;
import fish.crafting.logviewer.ui.component.SingleGridLayout;
import fish.crafting.logviewer.util.AdventureUtils;
import fish.crafting.logviewer.util.ColorUtil;
import fish.crafting.logviewer.util.TextUtil;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Getter @Accessors(fluent = true)
public class LogLine {

    public static int LINE_COMPONENT_HEIGHT = 9;

    @Setter
    private int lineId = -1;
    private final String message;
    private final String formattedMessage;
    private final LogLevel level;
    private final @Nullable Instant timestamp;
    @Setter
    private @Nullable LogPlugin plugin = null;

    public LogLine(String message, LogLevel level, @Nullable Instant timestamp) {
        if(message.isEmpty()) message = " ";
        this.message = message;
        this.formattedMessage = formatMessage(message);
        this.level = level;
        this.timestamp = timestamp;
    }

    private String formatMessage(String message){
        if(message.isEmpty()) return "";

        StringBuilder sb = new StringBuilder(message.length());

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);

            if (c == '\t') sb.append("    ");
            else if(c < 32 && c != '\n') {}
            else sb.append(c);
        }

        return sb.toString();
    }

    public String message() {
        return message;
    }

    public LogLevel level() {
        return level;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public Component component(){
        Text text = Text.of(formattedMessage);

        Style style = text.getStyle();
        style = style.withColor(level.getColor());
        text = text.getWithStyle(style).getFirst();

        var component = new HorizontalFlowLayout(Sizing.content(), Sizing.fixed(LINE_COMPONENT_HEIGHT));

        SingleGridLayout grid = new SingleGridLayout(Sizing.fixed(70), Sizing.fill());
        LogSideBarComponent side = new LogSideBarComponent(this);

        grid.child(side, 0, 0);
        side.update(grid, false);

        component.child(grid);

        LogLineComponent line = new LogLineComponent(text, lineId);
        line.sizing(Sizing.content(), Sizing.fill());
        component.child(line);

        return component;
    }

    public void onWritten(){

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LogLine) obj;
        return Objects.equals(this.message, that.message) &&
                Objects.equals(this.level, that.level) &&
                Objects.equals(this.timestamp, that.timestamp) &&
                Objects.equals(this.plugin, that.plugin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, level, timestamp, plugin);
    }

    @Override
    public String toString() {
        return "LogLine[" +
                "message=" + message + ", " +
                "level=" + level + ", " +
                "timestamp=" + timestamp + ", " +
                "plugin=" + plugin + ']';
    }

}
