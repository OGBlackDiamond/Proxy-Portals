package dev.ogblackdiamond.proxyportals;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

/**
 *  Main class for ProxyPortals.
 */
@Plugin(id = "proxyportals", name = "Proxy Portals", version = "1.0.0",
        description = "A portal plugin to transport players between servers.",
        authors = {"BlackDiamond"})
public class ProxyPortals {

    private final ProxyServer server;
    private final Logger logger;

    /**
     *  Constructor, initializes the constructor and proxy server.
     */
    @Inject
    public ProxyPortals(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        logger.info("Thank you for using ProxyPortals");
    }
}
