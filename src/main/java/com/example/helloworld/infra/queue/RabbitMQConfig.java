package com.example.helloworld.infra.queue;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "monitor.exchange";
    public static final String QUEUE = "incident.queue";
    public static final String ROUTING_KEY = "monitor.failed";

    @Bean
    TopicExchange monitorExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    Queue incidentQueue() {
        return new Queue(QUEUE);
    }

    @Bean
    Binding binding(Queue incidentQueue, TopicExchange monitorExchange) {
        return BindingBuilder.bind(incidentQueue).to(monitorExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
