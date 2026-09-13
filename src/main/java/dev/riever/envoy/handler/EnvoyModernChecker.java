package dev.riever.envoy.handler;

import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.riever.envoy.config.PlayerInfoForwarding;
import dev.riever.envoy.protocol.VelocityInternals;
import dev.riever.envoy.util.ReflectionUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.kyori.adventure.text.Component;

import java.lang.reflect.Method;
import java.util.Map;

public class EnvoyModernChecker extends ChannelInboundHandlerAdapter {

    private static final Component MODERN_FAIL = Component.translatable("velocity.error.modern-forwarding-failed");

    private final Map<String, PlayerInfoForwarding> serverConfig;

    public EnvoyModernChecker(Map<String, PlayerInfoForwarding> serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!isModern(ctx.channel())) {
            ctx.pipeline().remove(this);
            ctx.fireChannelRead(msg);
            return;
        }
        if (msg.getClass().getSimpleName().equals("ServerLoginSuccessPacket")) {
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
        String serverName = VelocityInternals.getServerName(ch);
        return PlayerInfoForwarding.MODERN.equals(serverConfig.get(serverName));
    }

    private void disconnect(Channel ch) throws Exception {
        ServerConnection serverConn = VelocityInternals.getVelocityServerConnection(ch);
        if (serverConn == null) {
            throw new IllegalStateException("ServerConnection is null");
        }
        Class<?> clazz = Class.forName("com.velocitypowered.proxy.connection.util.ConnectionRequestResults");
        Method method = clazz.getDeclaredMethod("forDisconnect", Component.class, RegisteredServer.class);
        Object result = method.invoke(null, MODERN_FAIL, serverConn.getServer());
        Object mc = ch.pipeline().get(VelocityInternals.Connections.HANDLER);
        Object handler = ReflectionUtils.invoke(mc, "getActiveSessionHandler");
        Object resultFuture = ReflectionUtils.get(handler, "resultFuture");
        ReflectionUtils.invoke(resultFuture, "complete", Object.class, result);
        ReflectionUtils.invoke(serverConn, "disconnect");
    }
}
