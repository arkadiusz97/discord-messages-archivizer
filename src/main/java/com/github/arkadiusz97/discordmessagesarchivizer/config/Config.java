package com.github.arkadiusz97.discordmessagesarchivizer.config;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableRetry
@EnableResilientMethods
public class Config {

    private final String deadLetterExchange;
    private final String deadLetterRoutingKey;
    private final String deadLetterQueue;

    public Config(@Value("${app.dead-letter-exchange}") String deadLetterExchange,
                  @Value("${app.dead-letter-routing-key}") String deadLetterRoutingKey,
                  @Value("${app.dead-letter-queue}") String deadLetterQueue) {
        this.deadLetterExchange = deadLetterExchange;
        this.deadLetterRoutingKey = deadLetterRoutingKey;
        this.deadLetterQueue = deadLetterQueue;
    }

    @Bean
    public JacksonJsonMessageConverter jsonConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter converter, @Value("${app.queue-prefetch-count}") Integer queuePrefetchCount) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(queuePrefetchCount);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setMessageConverter(converter);
        return factory;
    }

    @Bean
    public Queue mainQueue(@Value("${app.queue-name}") String queueName) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(deadLetterExchange)
                .deadLetterRoutingKey(deadLetterRoutingKey)
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(deadLetterExchange);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(deadLetterQueue).build();
    }

    @Bean
    public Binding expiredBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(deadLetterRoutingKey);
    }

    @Bean
    public GatewayDiscordClient gatewayDiscordClient(@Value("${app.discord-bot-token}") String discordBotToken) {
        return DiscordClientBuilder.create(discordBotToken)
                .build()
                .login()
                .block();
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor(
            @Value("${app.thread-pool-size-for-messages-handler}") int threadPoolSizeForMessagesHandler) {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setVirtualThreads(true);
        exec.setThreadNamePrefix("discord-messages-archivizer-handler-");
        exec.setCorePoolSize(threadPoolSizeForMessagesHandler);
        return exec;
    }

}
