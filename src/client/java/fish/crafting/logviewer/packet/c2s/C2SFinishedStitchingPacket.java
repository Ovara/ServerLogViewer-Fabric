package fish.crafting.logviewer.packet.c2s;

import fish.crafting.logviewer.packet.ByteBufPayload;
import fish.crafting.logviewer.packet.OutPacket;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutputStream;
import java.io.IOException;

public class C2SFinishedStitchingPacket extends OutPacket {

    @Override
    protected void writeBytes(@NotNull DataOutputStream outputStream) throws IOException {
        outputStream.writeByte(1);
    }

    @Override
    protected ByteBufPayload createPayload(@Nullable ByteBuf buf) {
        return new Payload(buf);
    }

    public static class Payload extends ByteBufPayload {

        public static final Id<ByteBufPayload> ID = getId("c2s_finished_stitching");

        protected Payload(@Nullable ByteBuf buf) {
            super(buf);
        }

        @Override
        public Id<ByteBufPayload> getByteBufID() {
            return ID;
        }
    }
}
