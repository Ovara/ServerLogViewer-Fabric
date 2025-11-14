package fish.crafting.logviewer.packet.s2c.log;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.log.file.LogFileManager;
import fish.crafting.logviewer.packet.ByteBufPayload;
import fish.crafting.logviewer.packet.InPacket;
import fish.crafting.logviewer.util.DebugUtil;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class S2CPluginIndexPacket extends InPacket {
    @Override
    protected void onMessageReceived(@NotNull DataInputStream stream) throws IOException {
        int indexBegin = stream.readInt();
        int n = stream.readInt();
        short[] ids = new short[n];
        for (int i = 0; i < n; i++) {
            short id = stream.readShort();
            ids[i] = id;
        }

        LogFileManager.get().getLogIndex().writePlugins(indexBegin, ids);
    }

    @Override
    protected ByteBufPayload createPayload(@Nullable ByteBuf buf) {
        return new Payload(buf);
    }

    public static class Payload extends ByteBufPayload {

        public static final Id<ByteBufPayload> ID = getId("s2c_plugin_index");

        protected Payload(@Nullable ByteBuf buf) {
            super(buf);
        }

        @Override
        public Id<ByteBufPayload> getByteBufID() {
            return ID;
        }
    }
}
