package fish.crafting.logviewer.packet.s2c.log;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.log.LogInstance;
import fish.crafting.logviewer.log.LogLevel;
import fish.crafting.logviewer.log.LogLine;
import fish.crafting.logviewer.log.LogPlugin;
import fish.crafting.logviewer.packet.ByteBufPayload;
import fish.crafting.logviewer.packet.InPacket;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class S2CLogLinesPacket extends InPacket {
    @Override
    protected void onMessageReceived(@NotNull DataInputStream stream) throws IOException {
        UUID id = UUID.fromString(stream.readUTF());
        int lines = stream.readInt();

        List<LogLine> newLines = new ArrayList<>();
        for (int i = 0; i < lines; i++) {
            String line = stream.readUTF();
            int logLevelOrdinal = stream.readByte();
            short plugin = stream.readShort();

            LogLevel[] values = LogLevel.values();
            if(logLevelOrdinal >= values.length || logLevelOrdinal < 0) continue;

            LogInstance logInstance = ConnectionManager.get().getLogInstance();
            LogPlugin logPlugin = plugin == -1 ? null : (logInstance == null ? null : logInstance.plugin(plugin));

            LogLevel level = LogLevel.values()[logLevelOrdinal];
            newLines.add(new LogLine(line, level, null).plugin(logPlugin));
        }

        if(newLines.isEmpty()) return;
        ConnectionManager.get().handleIncomingLogs(id, newLines);
    }

    @Override
    protected ByteBufPayload createPayload(@Nullable ByteBuf buf) {
        return new Payload(buf);
    }

    public static class Payload extends ByteBufPayload {

        public static final Id<ByteBufPayload> ID = getId("s2c_log_lines");

        protected Payload(@Nullable ByteBuf buf) {
            super(buf);
        }

        @Override
        public Id<ByteBufPayload> getByteBufID() {
            return ID;
        }
    }
}
