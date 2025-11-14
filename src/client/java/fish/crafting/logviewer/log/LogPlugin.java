package fish.crafting.logviewer.log;

import fish.crafting.logviewer.util.ColorUtil;
import lombok.Getter;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class LogPlugin {

    private static Map<String, List<Integer>> COLOR_MAP = new HashMap<>(){{
        put("Paper", List.of(0xFC4F56, 0x94D308, 0x428EDA, 0xF6E346));
        put("Spark", List.of(0xFCFC54));
    }};

    @Getter
    private final short id;
    private String name = null;
    private final List<Consumer<LogPlugin>> onResolved = new LinkedList<>();
    @Getter
    private int mainColor = 0xFFFFFFFF;
    @Getter
    private int alphaColor = ColorUtil.alpha(mainColor, 50);

    public LogPlugin(short id){
        this.id = id;
    }

    private void generateColors(){
        String pluginName = getPluginName();
        int color;

        List<Integer> preExisting = COLOR_MAP.get(pluginName);
        if(preExisting != null){
            color = preExisting.size() == 1 ? preExisting.getFirst() : preExisting.get(new Random().nextInt(preExisting.size()));
        }else{
            Random random = new Random(pluginName.hashCode());
            float hue = random.nextFloat();
            float saturation = random.nextFloat(0.3f, 0.75f);

            Color hsbColor = Color.getHSBColor(hue, saturation, 1f);
            color = hsbColor.getRGB();
        }

        this.mainColor = ColorUtil.alpha(color, 255);
        this.alphaColor = ColorUtil.alpha(mainColor, 50);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof LogPlugin logPlugin)) return false;
        return id == logPlugin.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public LogPlugin resolvePlugin(String name){
        if(resolved()) return this;

        this.name = name;
        for (Consumer<LogPlugin> consumer : onResolved) {
            consumer.accept(this);
        }

        generateColors();
        return this;
    }

    private boolean resolved(){
        return name != null;
    }

    public void attachOnResolved(Consumer<LogPlugin> consumer){
        if(resolved()) return;
        onResolved.add(consumer);
    }

    public String getPluginName(){
        return name == null ? ("Plugin #" + id) : name;
    }
}
