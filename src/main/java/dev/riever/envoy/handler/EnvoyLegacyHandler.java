package dev.riever.envoy.handler;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import dev.riever.envoy.config.PlayerInfoForwarding;
import dev.riever.envoy.protocol.PlayerDataForwarding;
import dev.riever.envoy.protocol.VelocityInternals;
import dev.riever.envoy.util.ReflectionUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import org.slf4j.Logger;

import java.util.Map;

public class EnvoyLegacyHandler extends ChannelOutboundHandlerAdapter {

    private final ProxyServer proxy;
    private final Map<String, PlayerInfoForwarding> serverConfig;
    private final Logger logger;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg != null && msg.getClass().getSimpleName().equals("HandshakePacket")) {
            try {
                String playerVhost = (String) ReflectionUtils.invoke(msg, "getServerAddress");
                ServerConnection serverConn = VelocityInternals.getVelocityServerConnection(ctx.channel());
                String fakeVhost = addLegacyForwarding(playerVhost, serverConn);
                ReflectionUtils.invoke(msg, "setServerAddress", String.class, fakeVhost);
            } catch (Throwable t) {
                // :P
            }
            ctx.pipeline().remove(this);
        }
        super.write(ctx, msg, promise);
    }

    public EnvoyLegacyHandler(ProxyServer proxy, Map<String, PlayerInfoForwarding> serverConfig, Logger logger) {
        this.proxy = proxy;
        this.serverConfig = serverConfig;
        this.logger = logger;
    }

    private String addLegacyForwarding(String playerVhost, ServerConnection serverConn) {
        if (serverConn != null) {
            String serverName = serverConn.getServerInfo().getName();
            PlayerInfoForwarding mode = serverConfig.getOrDefault(serverName, PlayerInfoForwarding.NONE);
            try {
                if (mode == PlayerInfoForwarding.LEGACY) {
                    return PlayerDataForwarding.createLegacyForwardingAddress(serverConn);
                } else if (mode == PlayerInfoForwarding.BUNGEEGUARD) {
                    byte[] secret = (byte[]) ReflectionUtils.invoke(this.proxy.getConfiguration(), "getForwardingSecret");
                    return PlayerDataForwarding.createBungeeGuardForwardingAddress(serverConn, secret);
                }
            } catch (Exception e) {
                logger.warn("Legacy forwarding address creation failed: ", e);
            }
        } else {
            logger.warn("ServerConnection object is null, forwarding player to default vhost");
        }
        return playerVhost;
    }
}
