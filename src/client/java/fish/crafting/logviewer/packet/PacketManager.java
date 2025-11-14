package fish.crafting.logviewer.packet;

import fish.crafting.logviewer.packet.c2s.C2SFinishedStitchingPacket;
import fish.crafting.logviewer.packet.c2s.C2SHandshakeConfirmPacket;
import fish.crafting.logviewer.packet.c2s.C2SRequestHandshakePacket;
import fish.crafting.logviewer.packet.s2c.handshake.S2CHandshakeStartPacket;
import fish.crafting.logviewer.packet.s2c.log.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PacketManager {

    private static PacketManager instance;
    private final Map<String, CustomPacket> packets = new HashMap<>();

    public static final C2SHandshakeConfirmPacket C2S_HANDSHAKE_CONFIRM = reg(new C2SHandshakeConfirmPacket());
    public static final C2SRequestHandshakePacket C2S_REQUEST_HANDSHAKE = reg(new C2SRequestHandshakePacket());
    public static final C2SFinishedStitchingPacket C2S_FINISHED_STITCHING = reg(new C2SFinishedStitchingPacket());

    private PacketManager(){
        instance = this;

        regIncomingPackets(
                new S2CHandshakeStartPacket(),
                new S2CInitiateLogPacket(),
                new S2CLogContentsPacket(),
                new S2CLogLinesPacket(),
                new S2CRegisterPluginsPacket(),
                new S2CPluginIndexPacket(),
                new S2CSetupStatePacket(),
                new S2CStopConnectionPacket()
        );

        ClientPlayConnectionEvents.INIT.register((a, b) -> {
            registerEverything();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((a, b) -> {
            unregisterEverything();
        });
    }

    public static PacketManager get(){
        return instance == null ? new PacketManager() : instance;
    }

    private static <T extends CustomPacket> T reg(@NotNull T packet){
        get().packets.put(packet.payload.getId().id().getPath(), packet);
        return packet;
    }

    /**
     * These packets have handlers in themselves and thus do not need to be stored as an object
     */
    private static void regIncomingPackets(InPacket... packets){
        for (InPacket packet : packets) {
            reg(packet);
        }
    }

    public @Nullable CustomPacket findPacket(@NotNull String idPath){
        return packets.get(idPath);
    }

    public void registerEverything(){
        this.packets.values().forEach(CustomPacket::register);
    }

    public void unregisterEverything(){
        this.packets.values().forEach(CustomPacket::unregister);
    }


}
