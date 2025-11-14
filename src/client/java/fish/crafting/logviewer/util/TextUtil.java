package fish.crafting.logviewer.util;

import net.kyori.adventure.text.Component;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TextUtil {

    public static Component coloredComponent(String s, @NotNull Formatting color) {
        Component text = Component.text(s);

        if(!color.isColor()){
            return text;
        }

        return text.color(net.kyori.adventure.text.format.TextColor.color(color.getColorValue()));
    }

    public static Component coloredComponent(String s, net.kyori.adventure.text.format.TextColor color) {
        Component text = Component.text(s);

        return text.color(color);
    }

    public static String wrap(String text, int maxLineLength){
        StringBuilder sb = new StringBuilder();
        int lineLength = 0;

        for (String word : text.split(" ")) {
            if(lineLength + word.length() > maxLineLength){
                sb.append("\n");
                lineLength = 0;
            }else if (lineLength > 0){
                sb.append(" ");
                lineLength++;
            }

            sb.append(word);
            lineLength += word.length();
        }

        return sb.toString();
    }

    public static Text colored(String s, Formatting color) {
        Text text = Text.of(s);
        Style style = text.getStyle().withColor(color);

        List<Text> withStyle = text.getWithStyle(style);
        return (withStyle == null || withStyle.isEmpty()) ? text : withStyle.getFirst();
    }

    public static Text colored(String s, TextColor color) {
        Text text = Text.of(s);
        Style style = text.getStyle().withColor(color);

        List<Text> withStyle = text.getWithStyle(style);
        return (withStyle == null || withStyle.isEmpty()) ? text : withStyle.get(0);
    }
}
