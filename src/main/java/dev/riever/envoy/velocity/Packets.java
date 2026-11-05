package dev.riever.envoy.velocity;

import static dev.riever.envoy.util.ReflectionUtils.invoke;

public class Packets {

    public static boolean isHandshake(Object msg) {
        return Handles.HANDSHAKE_PACKET.isInstance(msg);
    }

    public static boolean isServerLoginSuccess(Object msg) {
        return Handles.SERVER_LOGIN_SUCCESS_PACKET.isInstance(msg);
    }

    public static String getHandshakeServerAddress(Object handshake) {
        return (String) invoke(Handles.HANDSHAKE_GET_SERVER_ADDRESS, handshake);
    }

    public static void setHandshakeServerAddress(Object handshake, String address) {
        invoke(Handles.HANDSHAKE_SET_SERVER_ADDRESS, handshake, address);
    }
}
