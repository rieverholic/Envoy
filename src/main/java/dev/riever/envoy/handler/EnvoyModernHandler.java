package dev.riever.envoy.handler;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import dev.riever.envoy.protocol.PlayerDataForwarding;
import dev.riever.envoy.util.ReflectionUtils;
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

    public byte[] handle(ServerConnection serverConn, byte[] payload) throws Exception {
        int version = payload.length == 1 ? payload[0] : 1;
        byte[] secret = (byte[]) ReflectionUtils.invoke(this.proxy.getConfiguration(), "getForwardingSecret");
        return PlayerDataForwarding.createForwardingData(serverConn, secret, version);
    }

    public void setInfoForwarded(ServerConnection serverConn) {
        try {
            Object mc = ReflectionUtils.invoke(serverConn, "getConnection");
            if (mc == null) {
                return;
            }
            Channel channel = (Channel) ReflectionUtils.invoke(mc, "getChannel");
            if (channel == null) {
                return;
            }
            channel.attr(EnvoyModernHandler.INFO_FORWARDED).set(true);
        } catch (Throwable t) {
            logger.error("Failed to set info forwarded", t);
        }
    }
}
