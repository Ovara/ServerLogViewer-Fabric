package fish.crafting.logviewer.packet;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class ByteArrayPayload implements CustomPayload {

    private final Id<ByteArrayPayload> id;
    public byte[] data = null;

    public ByteArrayPayload(@NotNull String channel){
        Identifier identifier = Identifier.of("fqm", channel);
        this.id = new Id<>(identifier);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return this.id;
    }
}
