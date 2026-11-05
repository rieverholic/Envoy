package dev.riever.envoy.velocity;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.GameProfile;
import dev.riever.envoy.util.ReflectionUtils;
import io.netty.channel.ChannelInitializer;
import net.kyori.adventure.text.Component;

import java.lang.invoke.MethodHandle;

final class Handles {

    private Handles() {}

    private static final String PKG = "com.velocitypowered.proxy.";

    static final Class<?> MINECRAFT_CONNECTION = load("connection.MinecraftConnection");
    static final MethodHandle MC_GET_ASSOCIATION =
            getter(MINECRAFT_CONNECTION, "getAssociation");
    static final MethodHandle MC_GET_CHANNEL =
            getter(MINECRAFT_CONNECTION, "getChannel");
    static final MethodHandle MC_GET_ACTIVE_SESSION_HANDLER =
            getter(MINECRAFT_CONNECTION, "getActiveSessionHandler");

    static final MethodHandle SERVER_GET_CM = ReflectionUtils.fieldGetter(load("VelocityServer"), "cm");

    private static final Class<?> CONNECTION_MANAGER = load("network.ConnectionManager");
    static final MethodHandle CM_GET_BACKEND_HOLDER =
            getter(CONNECTION_MANAGER, "getBackendChannelInitializer");

    static final MethodHandle BACKEND_CI_HOLDER_SET =
            channelSetter(load("network.BackendChannelInitializerHolder"));

    static final MethodHandle CREATE_FORWARDING_DATA = ReflectionUtils.method(load("connection.PlayerDataForwarding"),
            "createForwardingData",
            byte[].class, String.class, ProtocolVersion.class, GameProfile.class, IdentifiedKey.class, int.class);

    private static final Class<?> CONFIGURATION = load("config.VelocityConfiguration");
    static final MethodHandle CFG_GET_FORWARDING_MODE = getter(CONFIGURATION, "getPlayerInfoForwardingMode");
    static final MethodHandle CFG_GET_FORWARDING_SECRET = getter(CONFIGURATION, "getForwardingSecret");

    static final Class<?> HANDSHAKE_PACKET = load("protocol.packet.HandshakePacket");
    static final MethodHandle HANDSHAKE_GET_SERVER_ADDRESS = getter(HANDSHAKE_PACKET, "getServerAddress");
    static final MethodHandle HANDSHAKE_SET_SERVER_ADDRESS =
            ReflectionUtils.method(HANDSHAKE_PACKET, "setServerAddress", String.class);
    static final Class<?> SERVER_LOGIN_SUCCESS_PACKET = load("protocol.packet.ServerLoginSuccessPacket");

    static final Class<?> LOGIN_SESSION_HANDLER = load("connection.backend.LoginSessionHandler");
    static final MethodHandle LOGIN_RESULT_FUTURE =
            ReflectionUtils.fieldGetter(LOGIN_SESSION_HANDLER, "resultFuture");
    static final MethodHandle FOR_DISCONNECT = ReflectionUtils.method(load("connection.util.ConnectionRequestResults"),
            "forDisconnect", Component.class, RegisteredServer.class);

    private static final Class<?> SERVER_CONNECTION = load("connection.backend.VelocityServerConnection");
    static final MethodHandle VSC_GET_CONNECTION = getter(SERVER_CONNECTION, "getConnection");
    static final MethodHandle VSC_DISCONNECT = ReflectionUtils.method(SERVER_CONNECTION, "disconnect");
    static final MethodHandle VSC_GET_PLAYER_REMOTE_ADDRESS =
            ReflectionUtils.declaredMethod(SERVER_CONNECTION, "getPlayerRemoteAddressAsString");
    static final MethodHandle VSC_CREATE_LEGACY_ADDRESS =
            ReflectionUtils.declaredMethod(SERVER_CONNECTION, "createLegacyForwardingAddress");
    static final MethodHandle VSC_CREATE_BUNGEEGUARD_ADDRESS = ReflectionUtils.declaredMethod(
            SERVER_CONNECTION, "createBungeeGuardForwardingAddress", byte[].class);

    private static Class<?> load(String name) {
        return ReflectionUtils.load(PKG + name, VelocitySupport.class.getClassLoader());
    }

    private static MethodHandle getter(Class<?> clazz, String name) {
        return ReflectionUtils.method(clazz, name);
    }

    private static MethodHandle channelSetter(Class<?> holder) {
        return ReflectionUtils.method(holder, "set", ChannelInitializer.class);
    }

    static void init() {}
}
