package dev.riever.envoy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerLoginPluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import dev.riever.envoy.config.EnvoyConfig;
import dev.riever.envoy.config.EnvoyConfigManager;
import dev.riever.envoy.config.PlayerInfoForwarding;
import dev.riever.envoy.handler.EnvoyModernHandler;
import dev.riever.envoy.inject.VelocityInjector;
import dev.riever.envoy.protocol.VelocityInternals;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Plugin(
        id = "envoy",
        name = "Envoy",
        version = "0.1.0",
        url = "https://github.com/rieverholic/Envoy",
        description = "A plugin that brings per-server information forwarding to Velocity",
        authors = {"Riever"}
)
public class Envoy {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private final Map<String, PlayerInfoForwarding> serverConfig;
    private EnvoyModernHandler handler;

    @Inject
    public Envoy(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        this.serverConfig = new HashMap<>();

        logger.info("Hello from Envoy!");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        if (VelocityInternals.getForwardConfig(this.server.getConfiguration()) != PlayerInfoForwarding.NONE) {
            logger.warn("Information forwarding must be disabled in root configuration! Envoy will be no-op.");
            return;
        }
        Path configFile = this.dataDirectory.resolve("config.yml");
        EnvoyConfigManager configManager = new EnvoyConfigManager(configFile, logger);
        EnvoyConfig config = configManager.initialize();
        for (EnvoyConfig.Server server : config.servers()) {
            this.serverConfig.put(server.name(), server.forwardingMode());
        }
        handler = new EnvoyModernHandler(this.server, this.logger);
        VelocityInjector injector = new VelocityInjector(this.server, this.logger, this.serverConfig);
        injector.inject();
    }

    @Subscribe
    public void onModernForwardRequest(ServerLoginPluginMessageEvent event) {
        ServerConnection connection = event.getConnection();
        String serverName = connection.getServerInfo().getName();
        String identifier = event.getIdentifier().getId();
        if (serverConfig.get(serverName) == PlayerInfoForwarding.MODERN && identifier.equals(VelocityInternals.INFO_CHANNEL)) {
            try {
                byte[] payload = handler.handle(connection, event.getContents());
                event.setResult(ServerLoginPluginMessageEvent.ResponseResult.reply(payload));
                handler.setInfoForwarded(connection);
            } catch (Exception e) {
                logger.error("Error handling modern forward request: ", e);
            }
        }
    }
}
