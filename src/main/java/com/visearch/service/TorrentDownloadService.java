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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.visearch.config.ConfigConstantsAndMethods.MAGNET_LINK_INITIALIAZED;
import static com.visearch.config.ConfigConstantsAndMethods.MAGNET_LINK_PROCESS_SUCCEEDED;
import static com.visearch.model.DHTModuleBuilder.buildDHTModule;

@Service
public class TorrentDownloadService {
    private static final Log logger = LogFactory.getLog(TorrentDownloadService.class);

    @Autowired
    private SearchForDownloadedContentService searchForDownloadedContentService;

    private ConcurrentHashMap<String, Integer> magnetLinkMap = new ConcurrentHashMap<>();
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

    }

    public void processDownloadingByMagnetLink(String magnetLink) {
        configureSecurity();

        if (magnetLink != null)
            clientBuilder.magnet(magnetLink);
        else
            throw new IllegalStateException("Torrent file or magnet URI is required");

        //todo: is setting id to each client needed?
        BtClient client = clientBuilder.build();
        logger.info("Starting new bit torrent client...");
        logger.info("New bit torrent client will download files from magnet_link = " + magnetLink);

        client.startAsync(state -> {
                boolean complete = (state.getPiecesRemaining() == 0);
                if (complete) {
                    client.stop();
                    magnetLinkMap.put(magnetLink, MAGNET_LINK_PROCESS_SUCCEEDED);
                    logger.info("Download Completed!");
                }
                //todo: is it necessary to throw TimeoutException?
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

    public void addMagnetToMap(String magnetLink) {
        magnetLinkMap.put(magnetLink, MAGNET_LINK_INITIALIAZED);
    }

    public void changeMagnetLinkStatus(String magnetLink, Integer status) {
        magnetLinkMap.put(magnetLink, status);
    }

    public String getNextInitializedLink() {
        return magnetLinkMap.entrySet().stream()
                .filter(entry -> (entry.getValue() == (int)MAGNET_LINK_INITIALIAZED))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

}


