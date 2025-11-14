package fish.crafting.logviewer.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ByteBufPayload implements CustomPayload {
    public final ByteBuf buf;

    protected ByteBufPayload(@Nullable ByteBuf buf){
        if(buf != null){
            int i = buf.readableBytes();
            this.buf = buf.readBytes(i);

        }else{
            this.buf = null;
        }
    }

    @Override
    public final Id<? extends CustomPayload> getId() {
        return getByteBufID();
    }

    public abstract Id<ByteBufPayload> getByteBufID();

    protected static Id<ByteBufPayload> getId(@NotNull String channel){
        return new Id<>(Identifier.of("slv", channel));
    }

    public static class CustomCodec implements PacketCodec<ByteBuf, ByteBufPayload> {

        private final Id<ByteBufPayload> id;

        public CustomCodec(@NotNull Id<ByteBufPayload> id){
            this.id = id;
        }

        @Override
        public ByteBufPayload decode(ByteBuf buf) {
            return new ByteBufPayload(buf) {
                @Override
                public Id<ByteBufPayload> getByteBufID() {
                    return id;
                }
            };
        }

        @Override
        public void encode(ByteBuf buf, ByteBufPayload value) {
            if(value.buf == null) return;

            buf.writeBytes(value.buf);
        }
    }
}
