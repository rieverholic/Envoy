package dev.riever.envoy.handler;

import com.velocitypowered.api.proxy.ServerConnection;
import dev.riever.envoy.config.PlayerInfoForwarding;
import dev.riever.envoy.velocity.Connections;
import dev.riever.envoy.velocity.Packets;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.kyori.adventure.text.Component;

import java.util.Map;

public class EnvoyModernChecker extends ChannelInboundHandlerAdapter {

    private static final Component MODERN_FAIL = Component.translatable("velocity.error.modern-forwarding-failed");

    private final Map<String, PlayerInfoForwarding> serverConfig;
    private final PlayerInfoForwarding defaultMode;

    public EnvoyModernChecker(Map<String, PlayerInfoForwarding> serverConfig, PlayerInfoForwarding defaultMode) {
        this.serverConfig = serverConfig;
        this.defaultMode = defaultMode != null ? defaultMode : PlayerInfoForwarding.NONE;
    }

    public EnvoyModernChecker(Map<String, PlayerInfoForwarding> serverConfig) {
        this(serverConfig, PlayerInfoForwarding.NONE);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!isModern(ctx.channel())) {
            ctx.pipeline().remove(this);
            ctx.fireChannelRead(msg);
            return;
        }
        if (Packets.isServerLoginSuccess(msg)) {
            if (Boolean.TRUE.equals(ctx.channel().attr(EnvoyModernHandler.INFO_FORWARDED).get())) {
                ctx.pipeline().remove(this);
                ctx.fireChannelRead(msg);
            } else {
                disconnect(ctx.channel());
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private boolean isModern(Channel ch) {
        String serverName = Connections.getServerName(ch);
        PlayerInfoForwarding mode = serverName == null ? defaultMode : serverConfig.getOrDefault(serverName, defaultMode);
        return PlayerInfoForwarding.MODERN.equals(mode);
    }

    private void disconnect(Channel ch) {
        ServerConnection serverConn = Connections.getVelocityServerConnection(ch);
        if (serverConn == null) {
            throw new IllegalStateException("ServerConnection is null");
        }
        Connections.disconnect(ch, serverConn, MODERN_FAIL);
    }
}
