package com.ronyt.csvmonitor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.http.async.SimpleSubscriber;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;


@Service
public class PlayersFileReader {

    private final ObjectMapper objectMapper;
    private final static Logger LOGGER = LoggerFactory.getLogger(PlayersFileReader.class);

//    private final S3AsyncClient s3AsyncClient;

    public PlayersFileReader(ObjectMapper objectMapper
//            , S3AsyncClient s3AsyncClient
    ) {
        this.objectMapper = objectMapper;
//        this.s3AsyncClient = s3AsyncClient;
    }

    //        @Value("${players_file.on_disk_full_path:c:\\intuit\\player.csv}")
//    private String playersFilePath;
    @Value("${players_bucket.name:ronyt-players}")
    private String playersBucketName;
    @Value("${players_file.file_name:player.csv}")
    private String playersFileName;

    @Value("${players_file.state_file_full_path:c:\\intuit\\db_state.json}")
    private String stateFilePath;

    @Synchronized
    public void readPlayersFile(PlayersDataHandler playersDataHandler) {

//        try (Stream<String> lines = java.nio.file.Files.lines(Path.of(playersFilePath), StandardCharsets.UTF_8)) {
//            lines.skip(1).parallel().forEach(lineHandler::handleLine);
//        } catch (IOException e) {
//            LOGGER.error("Error loading data state from file", e);
//        }

        S3AsyncClient s3AsyncClient = S3AsyncClient.create();
        final CompletableFuture<String> cf = new CompletableFuture<>();
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(playersBucketName)
                .key(playersFileName).build();

        s3AsyncClient.getObject(getObjectRequest, new AsyncResponseTransformer<GetObjectResponse, String>() {

            @Override
            public CompletableFuture<String> prepare() {
                return cf;
            }

            @Override
            public void onResponse(GetObjectResponse getObjectResponse) {
            }

            @Override
            public void onStream(SdkPublisher<ByteBuffer> sdkPublisher) {
                sdkPublisher.subscribe(new SimpleSubscriber(b -> {
                }) {
                    @Override
                    public void onNext(ByteBuffer byteBuffer) {
                        super.onNext(byteBuffer);
                        playersDataHandler.handleBuffer(byteBuffer);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        cf.complete("done");
                    }
                });

            }

            @Override
            public void exceptionOccurred(Throwable throwable) {
                System.out.println(throwable);
                LOGGER.error("Error while trying to get file from S3", throwable);
            }
        });
//                .thenApply(ResponseBytes::asUtf8String)
//                .whenComplete((stringContent, exception) -> {
//                    if (stringContent != null)
//                        System.out.println(stringContent);
//                    else
//                        exception.printStackTrace();
//                });
    }

    public boolean checkIfSyncNeeded() {
        boolean res = true;
        final CompletableFuture<Boolean> cf = new CompletableFuture<>();

        S3AsyncClient s3AsyncClient = S3AsyncClient.create();
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder().bucket(playersBucketName)
                .key(playersFileName).build();
        try {
            File stateFile = new File(stateFilePath);
            if (!stateFile.exists())
                return true;

            s3AsyncClient.headObject(headObjectRequest)
                    .whenComplete((stringContent, exception) -> {
                        cf.complete(stringContent.lastModified().getEpochSecond() * 1000 > stateFile.lastModified());
                    }).join();

            return cf.get().booleanValue();
        } catch (Exception e) {
            LOGGER.info("Error when checking s3 file");
            return true;
        }
        //        File stateFile = new File(stateFilePath);
//        return !stateFile.exists() || stateFile.lastModified() < new File(playersFilePath).lastModified();
    }

    public ConcurrentHashMap<String, String> getStateFileAsMap() {
        try (InputStream inputStream = new FileInputStream(stateFilePath)) {
            return new ObjectMapper().readValue(inputStream, ConcurrentHashMap.class);
        } catch (IOException e) {
            LOGGER.warn("Error loading state from file: %s", e);
            return new ConcurrentHashMap<>();
        }
    }

    public void writeStateToFile(Map<String, String> state) {
        try {
            objectMapper.writeValue(new File(stateFilePath), state);
        } catch (IOException e) {
            LOGGER.error("Error writing state to file", e);
        }
    }

}

