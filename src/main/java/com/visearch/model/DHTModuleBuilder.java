package com.visearch.model;

import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import com.google.inject.Module;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Optional;

public class DHTModuleBuilder {
    private static final Log logger = LogFactory.getLog(DHTModuleBuilder.class);

    private static final Integer DHT_PORT = 8055;

    public static Module buildDHTModule() {
        Optional<Integer> dhtPortOverride = tryGetPort(DHT_PORT);

        return new DHTModule(new DHTConfig() {
            @Override
            public int getListeningPort() {
                return dhtPortOverride.orElseGet(super::getListeningPort);
            }
            @Override
            public boolean shouldUseRouterBootstrap() {
                return true;
            }
        });
    }

    private static Optional<Integer> tryGetPort(final Integer port) {
        if (port == null) {
            return Optional.empty();

        } else if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("Invalid port: " + port + "; expected 1024..65535");

        }
        return Optional.of(port);

    }

}
