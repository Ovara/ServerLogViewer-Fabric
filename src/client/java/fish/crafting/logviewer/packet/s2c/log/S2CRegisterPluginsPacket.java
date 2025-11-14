package fish.crafting.logviewer.packet.s2c.log;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.log.LogInstance;
import fish.crafting.logviewer.packet.ByteBufPayload;
import fish.crafting.logviewer.packet.InPacket;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class S2CRegisterPluginsPacket extends InPacket {
    @Override
    protected void onMessageReceived(@NotNull DataInputStream stream) throws IOException {
        int n = stream.readInt();

        Map<String, Short> plugins = new HashMap<>();
        for (int i = 0; i < n; i++) {
            short id = stream.readShort();
            String name = stream.readUTF();

            plugins.put(name, id);
        }

        ConnectionManager.get().use(logInstance -> {
            logInstance.registerPlugins(plugins);
        });
    }

    @Override
    protected ByteBufPayload createPayload(@Nullable ByteBuf buf) {
        return new Payload(buf);
    }

    public static class Payload extends ByteBufPayload {

        public static final Id<ByteBufPayload> ID = getId("s2c_register_plugins");

        protected Payload(@Nullable ByteBuf buf) {
            super(buf);
        }

        @Override
        public Id<ByteBufPayload> getByteBufID() {
            return ID;
        }
    }
}
