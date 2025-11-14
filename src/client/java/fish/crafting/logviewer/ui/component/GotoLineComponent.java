package fish.crafting.logviewer.ui.component;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.log.LogInstance;
import fish.crafting.logviewer.ui.LogScreen;
import fish.crafting.logviewer.ui.LogViewerComponents;
import fish.crafting.logviewer.util.TextUtil;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.FocusHandler;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class GotoLineComponent extends ModernTextBoxComponent {
    public GotoLineComponent(Sizing horizontalSizing) {
        super(horizontalSizing);
        previewText("Goto...");
        setMaxLength(10);
        setTextPredicate(s -> {
            for (char c : s.toCharArray()) {
                if(c < '0' || c > '9') return false;
            }

            return true;
        });

        focusLost().subscribe(() -> {
            setText("");
        });

        keyPress().subscribe((key, scanCode, mods) -> {
            boolean removeFocus = key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER;

            if(!getText().isEmpty() && key == GLFW.GLFW_KEY_ENTER){
                String text = getText();
                try{
                    int line = Integer.parseInt(text);
                    LogScreen.use(logScreen -> {
                        logScreen.scrollToLine(line);
                    });

                    ConnectionManager.get().use(logInstance -> {
                        var current = logInstance.getViewerComponents().getCurrent();
                        for (Component child : current.children()) {
                            if(child instanceof VerticalFlowLayout logChunk){
                                LogLineComponent logLineComponent = logChunk.childById(LogLineComponent.class, "line" + line);
                                if(logLineComponent != null){
                                    logLineComponent.highlight();
                                    break;
                                }
                            }
                        }
                    });
                }catch (Exception ignored){}
            }

            FocusHandler focusHandler = focusHandler();
            if(removeFocus && focusHandler != null){
                focusHandler.focus(null, FocusSource.MOUSE_CLICK);
            }

            return true;
        });
    }

}
