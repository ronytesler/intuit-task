package com.ronyt.csvmonitor.service;

import java.nio.ByteBuffer;

// implemented by a class that can handle a line from the player.csv file
public interface PlayersDataHandler {
    void handleBuffer(ByteBuffer byteBuffer);
}
