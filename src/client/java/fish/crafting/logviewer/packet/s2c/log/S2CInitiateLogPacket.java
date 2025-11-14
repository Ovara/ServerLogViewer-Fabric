package fish.crafting.logviewer.packet.s2c.log;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.packet.ByteBufPayload;
import fish.crafting.logviewer.packet.InPacket;
import fish.crafting.logviewer.packet.s2c.handshake.S2CHandshakeStartPacket;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

public class S2CInitiateLogPacket extends InPacket {
    @Override
    protected void onMessageReceived(@NotNull DataInputStream stream) throws IOException {
        UUID uuid = UUID.fromString(stream.readUTF());
        int parts = stream.readInt();

        ConnectionManager.get().initiateLog(uuid, parts);
    }

    @Override
    protected ByteBufPayload createPayload(@Nullable ByteBuf buf) {
        return new Payload(buf);
    }

    public static class Payload extends ByteBufPayload {

        public static final Id<ByteBufPayload> ID = getId("s2c_log_data");

        protected Payload(@Nullable ByteBuf buf) {
            super(buf);
        }

        @Override
        public Id<ByteBufPayload> getByteBufID() {
            return ID;
        }
    }
}
