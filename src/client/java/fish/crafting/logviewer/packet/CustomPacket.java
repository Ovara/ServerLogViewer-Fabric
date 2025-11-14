package fish.crafting.logviewer.packet;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;

public abstract class CustomPacket {
    protected final ByteBufPayload payload;
    protected final ByteBufPayload.CustomCodec codec;

    protected CustomPacket() {
        this.payload = createPayload(null);
        this.codec = new ByteBufPayload.CustomCodec(this.payload.getByteBufID());
    }

    public abstract void unregister();

    public abstract void register();

    protected abstract ByteBufPayload createPayload(@Nullable ByteBuf buf);
}
