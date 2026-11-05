package dev.riever.envoy.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.config.ProxyConfig;
import dev.riever.envoy.config.PlayerInfoForwarding;
import io.netty.buffer.ByteBuf;

import static dev.riever.envoy.util.ReflectionUtils.invoke;

public class PlayerDataForwarding {

    public static final String INFO_CHANNEL = "velocity:player_info";

    public static String createLegacyForwardingAddress(ServerConnection serverConn) {
        return (String) invoke(Handles.VSC_CREATE_LEGACY_ADDRESS, serverConn);
    }

    public static String createBungeeGuardForwardingAddress(ServerConnection serverConn, byte[] secret) {
        return (String) invoke(Handles.VSC_CREATE_BUNGEEGUARD_ADDRESS, serverConn, secret);
    }

    public static byte[] createForwardingData(ServerConnection serverConn, byte[] secret, int version) {
        Player player = serverConn.getPlayer();
        String address = (String) invoke(Handles.VSC_GET_PLAYER_REMOTE_ADDRESS, serverConn);
        ByteBuf buf = (ByteBuf) invoke(Handles.CREATE_FORWARDING_DATA,
                secret,
                address,
                player.getProtocolVersion(),
                player.getGameProfile(),
                player.getIdentifiedKey(),
                version
        );
        try {
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            return data;
        } finally {
            buf.release();
        }
    }

    public static PlayerInfoForwarding getForwardConfig(ProxyConfig config) {
        Enum<?> mode = (Enum<?>) invoke(Handles.CFG_GET_FORWARDING_MODE, config);
        try {
            return PlayerInfoForwarding.valueOf(mode.name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static byte[] getForwardingSecret(ProxyConfig config) {
        return (byte[]) invoke(Handles.CFG_GET_FORWARDING_SECRET, config);
    }
}
