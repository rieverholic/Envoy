package dev.riever.envoy.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

import java.lang.invoke.MethodHandle;
import java.util.function.Supplier;

import static dev.riever.envoy.util.ReflectionUtils.invoke;

public class ChannelInitializers {

    public static ChannelInitializer<Channel> getBackendInitializer(ProxyServer server) {
        return getInitializerHolder(Handles.CM_GET_BACKEND_HOLDER, server).get();
    }

    public static void setBackendInitializer(ProxyServer server, ChannelInitializer<Channel> ci) {
        replaceInitializer(Handles.BACKEND_CI_HOLDER_SET, getInitializerHolder(Handles.CM_GET_BACKEND_HOLDER, server), ci);
    }

    @SuppressWarnings("unchecked")
    private static Supplier<ChannelInitializer<Channel>> getInitializerHolder(MethodHandle getHolderMethod, ProxyServer server) {
        return (Supplier<ChannelInitializer<Channel>>) invoke(getHolderMethod, invoke(Handles.SERVER_GET_CM, server));
    }

    private static void replaceInitializer(MethodHandle setMethod, Object holder, ChannelInitializer<Channel> ci) {
        invoke(setMethod, holder, ci);
    }
}
