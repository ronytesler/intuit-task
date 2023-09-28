package com.ronyt.csvmonitor;

import com.ronyt.csvmonitor.service.PlayersDataSynchronizer;
import com.ronyt.csvmonitor.service.PlayersFileReader;
import com.ronyt.csvmonitor.service.PlayersFileMonitor;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class CsvMonitorApplication {
    private static Logger LOGGER = LoggerFactory.getLogger(CsvMonitorApplication.class);

    private final PlayersFileReader playersFileReader;
    private final PlayersDataSynchronizer playersDataSynchronizer;
    private final PlayersFileMonitor playersFileMonitor;

    public CsvMonitorApplication(PlayersFileReader playersFileReader, PlayersDataSynchronizer playersDataSynchronizer,
                                 PlayersFileMonitor playersFileMonitor) {
        this.playersFileReader = playersFileReader;
        this.playersDataSynchronizer = playersDataSynchronizer;
        this.playersFileMonitor = playersFileMonitor;
    }

    public static void main(String[] args) {
        SpringApplication.run(CsvMonitorApplication.class, args);
    }

    @PostConstruct
    public void init() {
        if (!playersFileReader.checkIfSyncNeeded()) {
            return;
        }
        // read data state from file and keep it in memory
        playersDataSynchronizer.loadDataState();
        // read the players file and sync it with the memory state and the db
        playersDataSynchronizer.syncData();
    }

    // runs after init()
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            // monitor the player.csv file for any changes, and notify 'playersDataSynchronizer' that a change occurred
            playersFileMonitor.startMonitoring(playersDataSynchronizer);
        } catch (Exception e) {
            LOGGER.error("Error while monitoring players file", e);
        }
    }
}
