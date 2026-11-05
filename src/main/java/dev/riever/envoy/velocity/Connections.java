package dev.riever.envoy.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.netty.channel.Channel;
import net.kyori.adventure.text.Component;

import java.util.concurrent.CompletableFuture;

import static dev.riever.envoy.util.ReflectionUtils.invoke;

public class Connections {

    public static final String MINECRAFT_DECODER = "minecraft-decoder";
    public static final String MINECRAFT_ENCODER = "minecraft-encoder";
    public static final String HANDLER = "handler";

    public static ServerConnection getVelocityServerConnection(Channel ch) {
        Object mc = getConnection(ch);
        if (mc == null) {
            return null;
        }
        Object assoc = invoke(Handles.MC_GET_ASSOCIATION, mc);
        if (assoc instanceof ServerConnection sc) {
            return sc;
        }
        return null;
    }

    public static String getServerName(Channel ch) {
        Object mc = getConnection(ch);
        if (mc == null) {
            return null;
        }
        Object assoc = invoke(Handles.MC_GET_ASSOCIATION, mc);
        if (assoc instanceof ServerConnection sc) {
            return sc.getServerInfo().getName();
        } else if (assoc instanceof Player player) {
            return player.getCurrentServer()
                    .map(sc -> sc.getServerInfo().getName())
                    .orElse(null);
        }
        return null;
    }

    public static Channel getChannel(ServerConnection serverConn) {
        Object mc = invoke(Handles.VSC_GET_CONNECTION, serverConn);
        return mc == null ? null : (Channel) invoke(Handles.MC_GET_CHANNEL, mc);
    }

    private static Object getConnection(Channel ch) {
        Object handler = ch.pipeline().get(Connections.HANDLER);
        return Handles.MINECRAFT_CONNECTION.isInstance(handler) ? handler : null;
    }

    @SuppressWarnings("unchecked")
    public static void disconnect(Channel ch, ServerConnection serverConn, Component reason) {
        Object mc = getConnection(ch);
        if (mc == null) {
            throw new IllegalStateException("Connection does not exist");
        }
        Object handler = invoke(Handles.MC_GET_ACTIVE_SESSION_HANDLER, mc);
        if (!Handles.LOGIN_SESSION_HANDLER.isInstance(handler)) {
            throw new IllegalStateException("Connection not in the login phase");
        }
        RegisteredServer server = serverConn.getServer();
        Object result = invoke(Handles.FOR_DISCONNECT, reason, server);
        CompletableFuture<Object> resultFuture = (CompletableFuture<Object>) invoke(Handles.LOGIN_RESULT_FUTURE, handler);
        resultFuture.complete(result);
        invoke(Handles.VSC_DISCONNECT, serverConn);
    }
}
