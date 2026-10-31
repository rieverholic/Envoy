package dev.riever.envoy.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;

@ConfigSerializable
public record EnvoyConfig(
        @Setting(value = "default-forwarding-mode")
        @Comment("Default player information forwarding mode for servers not in the list.")
        PlayerInfoForwarding defaultForwardingMode,

        @Setting
        List<Server> servers
) {
    @ConfigSerializable
    public record Server(
        @Setting(value = "name")
        @Comment("Server name, as in Velocity config.")
        String name,

        @Setting(value = "forwarding-mode")
        @Comment("Player information forwarding mode.")
        PlayerInfoForwarding forwardingMode
    ) {
        public Server {
            if (forwardingMode == null) {
                forwardingMode = PlayerInfoForwarding.NONE;
            }
        }
    }

    public EnvoyConfig {
        if (defaultForwardingMode == null) {
            defaultForwardingMode = PlayerInfoForwarding.NONE;
        }
        if (servers == null) {
            servers = List.of();
        }
    }

    public EnvoyConfig() {
        this(PlayerInfoForwarding.NONE, List.of(new Server("", PlayerInfoForwarding.NONE)));
    }

    public EnvoyConfig(List<Server> servers) {
        this(PlayerInfoForwarding.NONE, servers);
    }
}
