package com.example.helloworld.infra.queue;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "monitor.exchange";
    public static final String DEAD_LETTER_EXCHANGE = "monitor.dlx";

    public static final String INCIDENT_DLQ = "incident.dlq";
    public static final String INCIDENT_DLQ_ROUTING_KEY = "monitor.failed.dlq";

    public static final String RESOLVED_INCIDENT_DLQ = "incident.resolved.dlq";
    public static final String RESOLVED_INCIDENT_DLQ_ROUTING_KEY = "monitor.failed.dlq";

    public static final String INCIDENT_QUEUE = "incident.queue";
    public static final String INCIDENT_ROUTING_KEY = "monitor.failed";

    public static final String RESOLVED_INCIDENT_QUEUE = "incident.resolved.queue";
    public static final String RESOLVED_ROUTING_KEY = "monitor.resolved";

    @Bean
    TopicExchange monitorExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    TopicExchange incidentExchange() {
        return new TopicExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    Queue incidentQueue() {
        return new Queue(INCIDENT_QUEUE, false, false, false, Map.of(
                "x-dead-letter-exchange", DEAD_LETTER_EXCHANGE,
                "x-dead-letter-routing-key", INCIDENT_DLQ_ROUTING_KEY
        ));
    }

    @Bean
    Queue incidentDeadLetterQueue() {
        return new Queue(INCIDENT_DLQ);
    }

    @Bean
    Queue resolvedQueue() {
        return new Queue(RESOLVED_INCIDENT_QUEUE);
    }

    @Bean
    Queue resolvedDeadLetterQueue() {
        return new Queue(RESOLVED_INCIDENT_DLQ, false, false, false,
                Map.of(
                        "x-dead-letter-exchange", DEAD_LETTER_EXCHANGE,
                        "x-dead-letter-routing-key", RESOLVED_INCIDENT_DLQ_ROUTING_KEY
                ));
    }

    @Bean
    Binding incidentBinding(Queue incidentQueue, TopicExchange monitorExchange) {
        return BindingBuilder.bind(incidentQueue).to(monitorExchange).with(INCIDENT_ROUTING_KEY);
    }

    @Bean
    Binding incidentDeadLetterBinding(Queue incidentDeadLetterQueue, TopicExchange incidentExchange) {
        return BindingBuilder.bind(incidentDeadLetterQueue).to(incidentExchange).with(INCIDENT_DLQ_ROUTING_KEY);
    }

    @Bean
    Binding resolvedBinding(Queue resolvedQueue, TopicExchange monitorExchange) {
        return BindingBuilder.bind(resolvedQueue).to(monitorExchange).with(RESOLVED_ROUTING_KEY);
    }

    @Bean
    Binding resolvedDeadLetterBinding(Queue resolvedDeadLetterQueue, TopicExchange incidentExchange) {
        return BindingBuilder.bind(resolvedDeadLetterQueue).to(incidentExchange).with(RESOLVED_INCIDENT_DLQ_ROUTING_KEY);
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

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());
        return factory;
    }
}
