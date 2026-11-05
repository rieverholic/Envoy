package dev.riever.envoy.handler;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import dev.riever.envoy.velocity.Connections;
import dev.riever.envoy.velocity.PlayerDataForwarding;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import org.slf4j.Logger;

public class EnvoyModernHandler {

    public static final AttributeKey<Boolean> INFO_FORWARDED = AttributeKey.valueOf("envoy-info-forwarded");

    private final ProxyServer proxy;
    private final Logger logger;

    public EnvoyModernHandler(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
    }

    public byte[] handle(ServerConnection serverConn, byte[] payload) {
        int version = payload.length == 1 ? payload[0] : 1;
        byte[] secret = PlayerDataForwarding.getForwardingSecret(proxy.getConfiguration());
        return PlayerDataForwarding.createForwardingData(serverConn, secret, version);
    }

    public void setInfoForwarded(ServerConnection serverConn) {
        try {
            Channel channel = Connections.getChannel(serverConn);
            if (channel != null) {
                channel.attr(INFO_FORWARDED).set(true);
            }
        } catch (RuntimeException e) {
            logger.error("Failed to set info forwarded", e);
        }
    }
}
