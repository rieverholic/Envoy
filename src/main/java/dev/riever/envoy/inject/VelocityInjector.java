package dev.riever.envoy.inject;

import com.velocitypowered.api.proxy.ProxyServer;
import dev.riever.envoy.config.PlayerInfoForwarding;
import dev.riever.envoy.util.ReflectionUtils;
import org.slf4j.Logger;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

import java.util.Map;

public class VelocityInjector {
    private final ProxyServer server;
    private final Logger logger;
    private final Map<String, PlayerInfoForwarding> serverConfig;
    private final PlayerInfoForwarding defaultMode;

    public VelocityInjector(
            ProxyServer server,
            Logger logger,
            Map<String, PlayerInfoForwarding> serverConfig,
            PlayerInfoForwarding defaultMode
    ) {
        this.server = server;
        this.logger = logger;
        this.serverConfig = serverConfig;
        this.defaultMode = defaultMode != null ? defaultMode : PlayerInfoForwarding.NONE;
    }

    public VelocityInjector(ProxyServer server, Logger logger, Map<String, PlayerInfoForwarding> serverConfig) {
        this(server, logger, serverConfig, PlayerInfoForwarding.NONE);
    }

    public void inject() {
        try {
            Object cm = ReflectionUtils.get(this.server, "cm");
            Object holder = ReflectionUtils.invoke(cm, "getBackendChannelInitializer");
            ChannelInitializer<?> original = (ChannelInitializer<?>) ReflectionUtils.invoke(holder, "get");
            ChannelInitializer<Channel> backendWrapped = new EnvoyChannelInitializer(
                    this.server, this.serverConfig, this.defaultMode, original, this.logger);
            ReflectionUtils.invoke(holder, "set", ChannelInitializer.class, backendWrapped);
        } catch (Throwable t) {
            logger.error("Reflection error: ", t);
        }
    }
}
