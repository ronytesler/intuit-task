package com.ronyt.csvmonitor.service;

import com.ronyt.csvmonitor.model.dto.PlayerDto;
import com.ronyt.csvmonitor.model.enums.Hand;
import com.ronyt.csvmonitor.publisher.RabbitMQProducer;
import com.ronyt.csvmonitor.util.HashingUtils;
import lombok.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlayersDataSynchronizer implements PlayersDataHandler, PlayersFileChangedHandler {
    private StringBuffer dataBuffer;

    private static Logger LOGGER = LoggerFactory.getLogger(PlayersDataSynchronizer.class);

    private ConcurrentHashMap<String, String> playerIdToHashedData;
    private final RabbitMQProducer rabbitMQProducer;
    private final PlayersFileReader playersFileReader;

    public PlayersDataSynchronizer(RabbitMQProducer rabbitMQProducer, PlayersFileReader playersFileReader) {
        this.rabbitMQProducer = rabbitMQProducer;
        this.playersFileReader = playersFileReader;
        dataBuffer = new StringBuffer(1000);
    }

    // read the db_state.json file and load it into the 'playerIdToHashedData' ConcurrentHashMap
    public void loadDataState() {
        this.playerIdToHashedData = playersFileReader.getStateFileAsMap();
        if (this.playerIdToHashedData == null) {
            playerIdToHashedData = new ConcurrentHashMap<>();
        }
    }

    // for each line
    @Synchronized
    public void syncData() {

        playersFileReader.readPlayersFile(byteBuffer -> {
            try {
                dataBuffer.append(StandardCharsets.UTF_8.decode(byteBuffer).toString());
                String[] lines = dataBuffer.toString().split(System.lineSeparator());
                Arrays.stream(lines, 0, lines.length - 1).parallel().forEach(line -> this.handleLine(line));
                dataBuffer.setLength(0);
                dataBuffer.insert(0, lines[lines.length - 1]);
            } catch (Exception e) {
                LOGGER.error(String.format("Exception while creating db state. Line: %s", e));
            }
        });
        playersFileReader.writeStateToFile(playerIdToHashedData);
    }

    public void handleLine(String line) {
        try {
            String playerID = line.substring(0, line.indexOf(","));
            String lineHashed = HashingUtils.hashText(line);
            String existingHash = playerIdToHashedData.get(playerID);
            if (existingHash != null && existingHash.equals(lineHashed)) {
                return;
            }
            // this player doesn't exist in our in memory db state,
            // or it exists but has changed. In both cases we update the memory and handle the line.
            String[] fields = line.split(",", -1);
            PlayerDto.PlayerDtoBuilder playerDtoBuilder = PlayerDto.builder();
            if (fields[0].isBlank()) {
                LOGGER.error(String.format("line without a playerId - %s", line));
                return;
            }
            playerDtoBuilder.playerID(fields[0]);
            if (!fields[1].isBlank())
                playerDtoBuilder.birthYear(Integer.parseInt(fields[1]));
            if (!fields[2].isBlank())
                playerDtoBuilder.birthMonth(Integer.parseInt(fields[2]));
            if (!fields[3].isBlank())
                playerDtoBuilder.birthDay(Integer.parseInt(fields[3]));
            if (!fields[4].isBlank())
                playerDtoBuilder.birthCountry(fields[4]);
            if (!fields[5].isBlank())
                playerDtoBuilder.birthState(fields[5]);
            if (!fields[6].isBlank())
                playerDtoBuilder.birthCity(fields[6]);
            if (!fields[7].isBlank())
                playerDtoBuilder.deathYear(Integer.parseInt(fields[7]));
            if (!fields[8].isBlank())
                playerDtoBuilder.deathMonth(Integer.parseInt(fields[8]));
            if (!fields[9].isBlank())
                playerDtoBuilder.deathDay(Integer.parseInt(fields[9]));
            if (!fields[10].isBlank())
                playerDtoBuilder.deathCountry(fields[10]);
            if (!fields[11].isBlank())
                playerDtoBuilder.deathState(fields[11]);
            if (!fields[12].isBlank())
                playerDtoBuilder.deathCity(fields[12]);
            if (!fields[13].isBlank())
                playerDtoBuilder.nameFirst(fields[13]);
            if (!fields[14].isBlank())
                playerDtoBuilder.nameLast(fields[14]);
            if (!fields[15].isBlank())
                playerDtoBuilder.nameGiven(fields[15]);
            if (!fields[16].isBlank())
                playerDtoBuilder.weight(Integer.parseInt(fields[16]));
            if (!fields[17].isBlank())
                playerDtoBuilder.height(Integer.parseInt(fields[17]));
            if (!fields[18].isBlank())
                playerDtoBuilder.bats(Hand.valueOf(fields[18]));
            if (!fields[19].isBlank())
                playerDtoBuilder.throwsHand(Hand.valueOf(fields[19]));
            if (!fields[20].isBlank())
                playerDtoBuilder.debut(fields[20]);
            if (!fields[21].isBlank())
                playerDtoBuilder.finalGame(fields[21]);
            if (!fields[22].isBlank())
                playerDtoBuilder.retroID(fields[22]);
            if (!fields[23].isBlank())
                playerDtoBuilder.bbrefID(fields[23]);

            rabbitMQProducer.sendMessage(playerDtoBuilder.build());
            playerIdToHashedData.put(playerID, lineHashed);

        } catch (Exception e) {
            LOGGER.error(String.format("Exception while processing line: %s", e));
        }
    }

    @Override
    public void handleBuffer(ByteBuffer byteBuffer) {

    }

    @Override
    public void handleFileChange() {
        syncData();
    }
}
