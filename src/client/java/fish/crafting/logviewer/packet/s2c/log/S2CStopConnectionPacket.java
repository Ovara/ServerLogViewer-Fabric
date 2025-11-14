package fish.crafting.logviewer.packet.s2c.log;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.packet.ByteBufPayload;
import fish.crafting.logviewer.packet.InPacket;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

public class S2CStopConnectionPacket extends InPacket {
    @Override
    protected void onMessageReceived(@NotNull DataInputStream stream) throws IOException {
        stream.readByte();
        ConnectionManager.get().handleDisconnect();
    }

    @Override
    protected ByteBufPayload createPayload(@Nullable ByteBuf buf) {
        return new Payload(buf);
    }

    public static class Payload extends ByteBufPayload {

        public static final Id<ByteBufPayload> ID = getId("s2c_stop_connection");

        protected Payload(@Nullable ByteBuf buf) {
            super(buf);
        }

        @Override
        public Id<ByteBufPayload> getByteBufID() {
            return ID;
        }
    }
}
