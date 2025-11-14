package fish.crafting.logviewer.packet;

import fish.crafting.logviewer.util.DebugUtil;
import fish.crafting.logviewer.util.Logs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.packet.CustomPayload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public abstract class InPacket extends CustomPacket {
    protected InPacket() {
        super();
        PayloadTypeRegistry.playS2C().register(this.payload.getByteBufID(), this.codec);
    }

    @Override
    public void unregister() {

    }

    @Override
    public void register() {
        ClientPlayNetworking.registerReceiver(this.payload.getByteBufID(), this::onMessageReceived);
    }

    private void onMessageReceived(CustomPayload customPayload, ClientPlayNetworking.Context context) {
        if(!(customPayload instanceof ByteBufPayload byteBufPayload)) return;
        ByteBuf buf = byteBufPayload.buf;

        byte[] byteArr = ByteBufUtil.getBytes(buf);

        if(byteArr == null) return;

        try(var byteStream = new ByteArrayInputStream(byteArr); var stream = new DataInputStream(byteStream)){
            String path = customPayload.getId().id().getPath();
            CustomPacket packet = PacketManager.get().findPacket(path);

            if(packet instanceof InPacket inPacket){
                inPacket.onMessageReceived(stream);
            }
        } catch (IOException e) {
            Logs.logError("Error while unpacking received packet!", e);
        }
    }

    protected abstract void onMessageReceived(@NotNull DataInputStream stream) throws IOException;
}
