package dev.riever.envoy.inject;

import com.velocitypowered.api.proxy.ProxyServer;
import dev.riever.envoy.config.PlayerInfoForwarding;
import dev.riever.envoy.handler.EnvoyLegacyHandler;
import dev.riever.envoy.handler.EnvoyModernChecker;
import dev.riever.envoy.protocol.VelocityInternals;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import java.lang.reflect.Method;
import java.util.Map;
import org.slf4j.Logger;

public class EnvoyChannelInitializer extends ChannelInitializer<Channel> {
    private static final Method INIT_CHANNEL;
    static {
        try {
            INIT_CHANNEL = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            INIT_CHANNEL.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final ProxyServer proxy;
    private final Map<String, PlayerInfoForwarding> serverConfig;
    private final ChannelInitializer<?> delegate;
    private final Logger logger;

    public EnvoyChannelInitializer(
            ProxyServer proxy,
            Map<String, PlayerInfoForwarding> serverConfig,
            ChannelInitializer<?> delegate,
            Logger logger
    ) {
        this.proxy = proxy;
        this.serverConfig = serverConfig;
        this.delegate = delegate;
        this.logger = logger;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        INIT_CHANNEL.invoke(this.delegate, channel);
        channel.pipeline().addAfter(
                VelocityInternals.Connections.MINECRAFT_ENCODER,
                "envoy-legacy-forward",
                new EnvoyLegacyHandler(this.proxy, this.serverConfig, this.logger)
        );
        channel.pipeline().addAfter(
                VelocityInternals.Connections.MINECRAFT_DECODER,
                "envoy-modern-checker",
                new EnvoyModernChecker(this.serverConfig)
        );
    }
}
