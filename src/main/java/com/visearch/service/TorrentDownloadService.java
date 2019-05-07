package com.visearch.service;

import bt.Bt;
import bt.BtClientBuilder;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.protocol.crypto.EncryptionPolicy;
import bt.runtime.BtClient;
import bt.runtime.BtRuntime;
import bt.runtime.Config;
import bt.torrent.selector.PieceSelector;
import bt.torrent.selector.SequentialSelector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.security.Security;
import java.util.Optional;

import static com.visearch.model.DHTModuleBuilder.buildDHTModule;

@Service
public class TorrentDownloadService {
    private static final Log logger = LogFactory.getLog(TorrentDownloadService.class);

    @Autowired
    private SearchForDownloadedContentService searchForDownloadedContentService;
    private BtClientBuilder clientBuilder;

    TorrentDownloadService() {
        Config config = new Config() {
            @Override
            public int getNumOfHashingThreads() {
                return Runtime.getRuntime().availableProcessors();
            }
            @Override
            public EncryptionPolicy getEncryptionPolicy() {
                return EncryptionPolicy.PREFER_PLAINTEXT;
            }
        };

        BtRuntime runtime = BtRuntime.builder(config)
                .module(buildDHTModule())
                .autoLoadModules()
                .build();
        Path targetDirectory = new File("Downloads").toPath();
        Storage storage = new FileSystemStorage(targetDirectory);

        PieceSelector selector = SequentialSelector.sequential();

        clientBuilder = Bt.client(runtime)
                .storage(storage)
                .selector(selector);

        clientBuilder.afterTorrentFetched(System.out::println); //todo: set normal afterparty Consumers
        clientBuilder.afterFilesChosen(System.out::println);

    }

    public void processDownloadingByMagnetLink(String magnetLink) {
        configureSecurity();

        if (magnetLink != null)
            clientBuilder.magnet(magnetLink);
        else
            throw new IllegalStateException("Torrent file or magnet URI is required");

        BtClient client = clientBuilder.build();
        logger.info("Starting new bit torrent client...");
        client.startAsync(state -> {
                boolean complete = (state.getPiecesRemaining() == 0);
                if (complete) {
                    client.stop();
                    logger.info("Download Completed!");
                }
            }, 1000).join();

    }

    private static void configureSecurity() {
        String key = "crypto.policy";
        String value = "unlimited";
        try {
            Security.setProperty(key, value);
        } catch (Exception e) {
            logger.error(String.format("Failed to set security property '%s' to '%s'", key, value), e);
        }
    }

    public static Optional<Integer> tryGetPort(final Integer port) {
        if (port == null) {
            return Optional.empty();

        } else if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("Invalid port: " + port + "; expected 1024..65535");

        }
        return Optional.of(port);
    }
}
