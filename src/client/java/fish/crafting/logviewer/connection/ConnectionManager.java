package fish.crafting.logviewer.connection;

import fish.crafting.logviewer.ServerLogViewerClient;
import fish.crafting.logviewer.log.LogInstance;
import fish.crafting.logviewer.log.LogLine;
import fish.crafting.logviewer.log.file.LogFileManager;
import fish.crafting.logviewer.packet.PacketManager;
import fish.crafting.logviewer.ui.IncompatibleScreen;
import fish.crafting.logviewer.ui.LogScreen;
import lombok.Getter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ConnectionManager {

    private static ConnectionManager instance;
    private boolean validConnection = false;
    @Getter
    private LogInstance logInstance = null;
    @Getter
    public int pluginVersion = -1;
    @Getter
    private boolean notSetup = false;

    private ConnectionManager(){
        instance = this;

        ClientPlayConnectionEvents.DISCONNECT.register((a, b) -> {
            handleDisconnect();
        });
    }

    public static ConnectionManager get(){
        return instance == null ? new ConnectionManager() : instance;
    }

    public void handleHandshake(int compatibilityVersion){
        notSetup = false;

        if(compatibilityVersion != ServerLogViewerClient.COMPATIBILITY_VERSION){
            this.pluginVersion = compatibilityVersion;

            if(MinecraftClient.getInstance().currentScreen instanceof LogScreen){
                MinecraftClient.getInstance().setScreen(new IncompatibleScreen());
            }

            return;
        }

        this.pluginVersion = -1;
        this.validConnection = true;
        PacketManager.C2S_HANDSHAKE_CONFIRM.sendPacket();
    }

    public boolean incompatible(){
        return pluginVersion != -1;
    }

    public void initiateLog(UUID id, int parts){
        use(LogInstance::shutdown);
        this.logInstance = new LogInstance(id);
        this.logInstance.beginLogStitch(parts);
    }

    public void handleDisconnect(){
        this.notSetup = false;
        this.validConnection = false;
        use(LogInstance::shutdown);
        this.logInstance = null;

        LogFileManager.get().shutdown();
    }

    public void handleLogContents(UUID id, int stitchIndex, byte[] bytes) {
        use(id, log -> {
            log.handleStitchPart(stitchIndex, bytes);
        });
    }

    public void handleIncomingLogs(UUID id, List<LogLine> lines) {
        use(id, log -> {
            log.addLines(lines);
        });
    }

    public void use(@NotNull Consumer<LogInstance> consumer){
        useOr(consumer, null);
    }

    public void useOr(@NotNull Consumer<LogInstance> use, @Nullable Runnable or){
        if(validConnection && logInstance != null) use.accept(logInstance);
        else {
            if(or != null) or.run();
        }
    }

    private void use(UUID id, Consumer<LogInstance> consumer){
        use(l -> {
            if(l.getId().equals(id)) consumer.accept(l);
        });
    }

    public void setupState(boolean setup) {
        notSetup = !setup;
    }
}
