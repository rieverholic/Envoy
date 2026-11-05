package dev.riever.envoy.handler;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import dev.riever.envoy.config.PlayerInfoForwarding;
import dev.riever.envoy.velocity.Connections;
import dev.riever.envoy.velocity.Packets;
import dev.riever.envoy.velocity.PlayerDataForwarding;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import org.slf4j.Logger;

import java.util.Map;

public class EnvoyLegacyHandler extends ChannelOutboundHandlerAdapter {

    private final ProxyServer proxy;
    private final Map<String, PlayerInfoForwarding> serverConfig;
    private final PlayerInfoForwarding defaultMode;
    private final Logger logger;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (Packets.isHandshake(msg)) {
            try {
                String playerVhost = Packets.getHandshakeServerAddress(msg);
                ServerConnection serverConn = Connections.getVelocityServerConnection(ctx.channel());
                String fakeVhost = addLegacyForwarding(playerVhost, serverConn);
                Packets.setHandshakeServerAddress(msg, fakeVhost);
            } catch (RuntimeException e) {
                logger.warn("Failed to rewrite the handshake address: ", e);
            }
            ctx.pipeline().remove(this);
        }
        super.write(ctx, msg, promise);
    }

    public EnvoyLegacyHandler(
            ProxyServer proxy,
            Map<String, PlayerInfoForwarding> serverConfig,
            PlayerInfoForwarding defaultMode,
            Logger logger
    ) {
        this.proxy = proxy;
        this.serverConfig = serverConfig;
        this.defaultMode = defaultMode != null ? defaultMode : PlayerInfoForwarding.NONE;
        this.logger = logger;
    }

    public EnvoyLegacyHandler(ProxyServer proxy, Map<String, PlayerInfoForwarding> serverConfig, Logger logger) {
        this(proxy, serverConfig, PlayerInfoForwarding.NONE, logger);
    }

    private String addLegacyForwarding(String playerVhost, ServerConnection serverConn) {
        if (serverConn != null) {
            String serverName = serverConn.getServerInfo().getName();
            PlayerInfoForwarding mode = serverConfig.getOrDefault(serverName, this.defaultMode);
            try {
                if (mode == PlayerInfoForwarding.LEGACY) {
                    return PlayerDataForwarding.createLegacyForwardingAddress(serverConn);
                } else if (mode == PlayerInfoForwarding.BUNGEEGUARD) {
                    byte[] secret = (byte[]) PlayerDataForwarding.getForwardingSecret(this.proxy.getConfiguration());
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
