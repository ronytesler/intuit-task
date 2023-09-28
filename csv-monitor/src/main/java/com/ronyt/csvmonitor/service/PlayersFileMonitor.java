package com.ronyt.csvmonitor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;

//@Lazy(false)
@Service
public class PlayersFileMonitor {
    private static Logger LOGGER = LoggerFactory.getLogger(PlayersFileMonitor.class);

    @Value("${players_file.on_disk_full_path:c:\\intuit\\player.csv}")
    private String playersFilePath;

    @EventListener(ApplicationReadyEvent.class)

    public void startMonitoring(PlayersFileChangedHandler playersFileChangedHandler) throws IOException, InterruptedException {
        LOGGER.info("Started monitoring players file");
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path path = Paths.get(playersFilePath);
        Path folder = path.getParent();
        Path file = path.getFileName();
        LOGGER.info("Monitoring in folder " + folder.toString());

        folder.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        WatchKey key;

        while ((key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                LOGGER.info("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
                if (((Path) event.context()).getFileName().endsWith(file)) {
                    LOGGER.info("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
                    playersFileChangedHandler.handleFileChange();
                }
            }
            key.reset();
        }

        watchService.close();
    }
}
