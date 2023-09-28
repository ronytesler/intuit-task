package com.ronyt.hometask.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.name:message_queue}")
    private String queueName;
    @Value("${rabbitmq.exchange.name:message_exchange}")
    private String exchangeName;
    @Value("${rabbitmq.routing_key.name:routing_key}")
    private String routingKeyName;

    @Bean
    Queue queue() {
        return new Queue(queueName);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    Binding binding() {
        return BindingBuilder.bind(queue())
                .to(exchange())
                .with(routingKeyName);
    }

    @Bean
    MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
