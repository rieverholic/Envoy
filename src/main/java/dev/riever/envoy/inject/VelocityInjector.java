package dev.riever.envoy.inject;

import com.velocitypowered.api.proxy.ProxyServer;
import dev.riever.envoy.config.PlayerInfoForwarding;
import dev.riever.envoy.velocity.ChannelInitializers;
import dev.riever.envoy.velocity.VelocitySupport;
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
        ChannelInitializer<Channel> original = ChannelInitializers.getBackendInitializer(server);
        ChannelInitializer<Channel> wrapped = new EnvoyChannelInitializer(
                this.server, this.serverConfig, this.defaultMode, original, this.logger);
        ChannelInitializers.setBackendInitializer(server, wrapped);
    }
}
