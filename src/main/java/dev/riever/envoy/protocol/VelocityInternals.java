package dev.riever.envoy.protocol;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.config.ProxyConfig;
import dev.riever.envoy.config.PlayerInfoForwarding;
import dev.riever.envoy.util.ReflectionUtils;
import io.netty.channel.Channel;

public final class VelocityInternals {
    public static class Connections {
        public static final String MINECRAFT_DECODER = "minecraft-decoder";
        public static final String MINECRAFT_ENCODER = "minecraft-encoder";
        public static final String HANDLER = "handler";
    }

    public static final String INFO_CHANNEL = "velocity:player_info";

    public static ServerConnection getVelocityServerConnection(Channel ch) {
        Object mc = ch.pipeline().get(Connections.HANDLER);
        if (mc == null) {
            return null;
        }
        try {
            Object assoc = ReflectionUtils.invoke(mc, "getAssociation");
            if (assoc instanceof ServerConnection sc) {
                return sc;
            }
            return null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    public static String getServerName(Channel ch) {
        Object mc = ch.pipeline().get(Connections.HANDLER);
        if (mc == null) {
            return null;
        }
        try {
            Object assoc = ReflectionUtils.invoke(mc, "getAssociation");
            if (assoc instanceof ServerConnection sc) {
                return sc.getServerInfo().getName();
            } else if (assoc instanceof Player player) {
                return player.getCurrentServer()
                        .map(sc -> sc.getServerInfo().getName())
                        .orElse(null);
            }
            return null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    public static PlayerInfoForwarding getForwardConfig(ProxyConfig config) {
        try {
            Enum<?> mode = (Enum<?>) ReflectionUtils.invoke(config, "getPlayerInfoForwardingMode");
            return PlayerInfoForwarding.valueOf(mode.name());
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            return PlayerInfoForwarding.NONE;
        }
    }
}
