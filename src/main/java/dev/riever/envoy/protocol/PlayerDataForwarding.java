package dev.riever.envoy.protocol;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.api.util.GameProfile;
import dev.riever.envoy.util.ReflectionUtils;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Method;

public class PlayerDataForwarding {

    private static final Method CREATE_MODERN_FORWARDING;

    static {
        try {
            Class<?> clazz = Class.forName("com.velocitypowered.proxy.connection.PlayerDataForwarding");
            CREATE_MODERN_FORWARDING = clazz.getDeclaredMethod(
                    "createForwardingData",
                    byte[].class,
                    String.class,
                    ProtocolVersion.class,
                    GameProfile.class,
                    IdentifiedKey.class,
                    int.class
            );
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static String createLegacyForwardingAddress(ServerConnection serverConn) throws Exception {
        return (String) ReflectionUtils.invoke(serverConn, "createLegacyForwardingAddress");
    }

    public static String createBungeeGuardForwardingAddress(ServerConnection serverConn, byte[] secret) throws Exception {
        return (String) ReflectionUtils.invoke(serverConn, "createBungeeGuardForwardingAddress", byte[].class, secret);
    }

    public static byte[] createForwardingData(ServerConnection serverConn, byte[] secret, int version) throws Exception {
        Player player = serverConn.getPlayer();
        String address = (String) ReflectionUtils.invoke(serverConn, "getPlayerRemoteAddressAsString");
        ByteBuf buf = (ByteBuf) CREATE_MODERN_FORWARDING.invoke(
                null,
                secret,
                address,
                player.getProtocolVersion(),
                player.getGameProfile(),
                player.getIdentifiedKey(),
                version
        );
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        buf.release();
        return data;
    }
}
