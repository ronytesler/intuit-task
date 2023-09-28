package com.ronyt.csvmonitor.publisher;

import com.ronyt.csvmonitor.model.dto.PlayerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {
    private static Logger LOGGER = LoggerFactory.getLogger(RabbitMQProducer.class);

    @Value("${rabbitmq.exchange.name:message_exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routing_key.name:routing_key}")
    private String routingKeyName;

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(PlayerDto playerDto) {
        LOGGER.info(String.format("Sending message: %s", playerDto.getPlayerID()));
        rabbitTemplate.convertAndSend(exchangeName, routingKeyName, playerDto);
    }
}
