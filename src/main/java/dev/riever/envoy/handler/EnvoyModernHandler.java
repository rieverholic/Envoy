package dev.riever.envoy.handler;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.api.util.GameProfile;
import dev.riever.envoy.util.ReflectionUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import org.slf4j.Logger;

import java.lang.reflect.Method;

public class EnvoyModernHandler {

    public static final AttributeKey<Boolean> INFO_FORWARDED = AttributeKey.valueOf("envoy-info-forwarded");

    private final ProxyServer proxy;
    private final Logger logger;

    public EnvoyModernHandler(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
    }

    public byte[] handle(ServerConnection serverConn, byte[] payload) throws Exception {
        int version = payload.length == 1 ? payload[0] : 1;
        Player player = serverConn.getPlayer();
        byte[] secret = (byte[]) ReflectionUtils.invoke(this.proxy.getConfiguration(), "getForwardingSecret");
        String address = (String) ReflectionUtils.invoke(serverConn, "getPlayerRemoteAddressAsString");
        Class<?> clazz = Class.forName("com.velocitypowered.proxy.connection.PlayerDataForwarding");
        Method method = clazz.getDeclaredMethod(
                "createForwardingData",
                byte[].class,
                String.class,
                ProtocolVersion.class,
                GameProfile.class,
                IdentifiedKey.class,
                int.class
        );
        ByteBuf buf = (ByteBuf) method.invoke(
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

    public void setInfoForwarded(ServerConnection serverConn) {
        try {
            Object mc = ReflectionUtils.invoke(serverConn, "getConnection");
            if (mc == null) {
                return;
            }
            Channel channel = (Channel) ReflectionUtils.invoke(mc, "getChannel");
            if (channel == null) {
                return;
            }
            channel.attr(EnvoyModernHandler.INFO_FORWARDED).set(true);
        } catch (Throwable t) {
            logger.error("Failed to set info forwarded", t);
        }
    }
}
