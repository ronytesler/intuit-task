package com.ronyt.hometask.consumer;

import com.ronyt.hometask.model.dto.PlayerDto;
import com.ronyt.hometask.service.PlayerMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;


@Service
public class RabbitMQConsumer {
    private static Logger LOGGER = LoggerFactory.getLogger(RabbitMQConsumer.class);

    private final PlayerMessageHandler playerMessageHandler;

    public RabbitMQConsumer(PlayerMessageHandler playerMessageHandler) {
        this.playerMessageHandler = playerMessageHandler;
    }

    // handle a message with a player data that needs to be saved to the db
    @RabbitListener(queues = {"${rabbitmq.queue.name:message_queue}"})
    public void consume(PlayerDto playerDto) {
        LOGGER.info(String.format("Got player: %s", playerDto.getPlayerID()));
        playerMessageHandler.handlePlayerMessage(playerDto);
    }
}
