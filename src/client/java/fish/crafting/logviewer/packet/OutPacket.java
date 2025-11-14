package fish.crafting.logviewer.packet;

import fish.crafting.logviewer.util.DebugUtil;
import fish.crafting.logviewer.util.Logs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class OutPacket extends CustomPacket {

    public OutPacket(){
        super();
        PayloadTypeRegistry.playC2S().register(this.payload.getByteBufID(), this.codec);
    }

    @Override
    public void unregister() {

    }

    @Override
    public void register() {

    }

    public final void sendPacket(){
        byte[] info = null;
        try(var byteStream = new ByteArrayOutputStream(); var stream = new DataOutputStream(byteStream)){
            writeBytes(stream);
            info = byteStream.toByteArray();
        }catch (IOException e){
            Logs.logError("Error while packing packet!", e);
            return;
        }

        if(info.length == 0) return;

        ByteBuf byteBuf = Unpooled.copiedBuffer(info);
        ByteBufPayload newPayload = createPayload(byteBuf);
        ClientPlayNetworking.send(newPayload);
    }

    protected abstract void writeBytes(@NotNull DataOutputStream outputStream) throws IOException;
}
